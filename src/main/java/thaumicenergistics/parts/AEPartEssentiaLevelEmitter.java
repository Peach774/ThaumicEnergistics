package thaumicenergistics.parts;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.container.ContainerPartEssentiaLevelEmitter;
import thaumicenergistics.fluids.GaseousEssentia;
import thaumicenergistics.gui.GuiEssentiaLevelEmitter;
import thaumicenergistics.network.IAspectSlotPart;
import thaumicenergistics.network.packet.PacketEssentiaEmitter;
import thaumicenergistics.network.packet.PacketAspectSlot;
import thaumicenergistics.registries.AEPartsEnum;
import thaumicenergistics.texture.BlockTextureManager;
import thaumicenergistics.util.EssentiaConversionHelper;
import thaumicenergistics.util.EssentiaItemContainerHelper;
import appeng.api.config.RedstoneMode;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import com.google.common.collect.Lists;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class AEPartEssentiaLevelEmitter extends AEPartBase implements IStackWatcherHost, IAspectSlotPart
{
	/**
	 * How much AE power is required to keep the part active.
	 */
	private static final double IDLE_POWER_DRAIN = 0.5D;
	
	private Aspect filterAspect;
	private RedstoneMode mode = RedstoneMode.HIGH_SIGNAL;
	private IStackWatcher watcher;
	private long wantedAmount;
	private long currentAmount;

	public AEPartEssentiaLevelEmitter()
	{
		super( AEPartsEnum.EssentiaLevelEmitter );
	}

	private boolean isPowering()
	{
		switch ( this.mode )
		{
			case HIGH_SIGNAL:
				return this.wantedAmount <= this.currentAmount;

			case LOW_SIGNAL:
				return this.wantedAmount >= this.currentAmount;

			case IGNORE:
			case SIGNAL_PULSE:
				break;

		}

		return false;
	}

	private void notifyNeighborsOfChange()
	{
		this.tile.getWorldObj().notifyBlocksOfNeighborChange( this.tile.xCoord, this.tile.yCoord, this.tile.zCoord, Blocks.air );

		this.tile.getWorldObj().notifyBlocksOfNeighborChange( this.tile.xCoord + this.cableSide.offsetX, this.tile.yCoord + this.cableSide.offsetX,
			this.tile.zCoord + this.cableSide.offsetX, Blocks.air );
	}

	private void queryNetworkForCurrentAmount()
	{
		if ( this.filterAspect != null )
		{
			// Get the gas for this aspect
			GaseousEssentia essentiaGas = GaseousEssentia.getGasFromAspect( this.filterAspect );

			// Ask how much is in the network
			this.setCurrentAmount( EssentiaConversionHelper.convertFluidAmountToEssentiaAmount( this.checkGasAmount( essentiaGas ) ) );
		}
	}

	private void setCurrentAmount( long amount )
	{
		if( amount != this.currentAmount )
		{
			this.currentAmount = amount;
	
			// Do we have a grid node?
			if ( this.node != null )
			{
				// Set active based on grid node
				this.isActive = this.node.isActive();
	
				// Mark the host tile for an update
				this.host.markForUpdate();
	
				// Notify our block, and the block we are facing the we have
				// changed
				this.notifyNeighborsOfChange();
			}
		}
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 8;
	}

	public void changeWantedAmount( int modifier, EntityPlayer player )
	{
		this.setWantedAmount( this.wantedAmount + modifier, player );
	}

	@Override
	public void getBoxes( IPartCollsionHelper helper )
	{
		helper.addBox( 7.0D, 7.0D, 11.0D, 9.0D, 9.0D, 16.0D );
	}

	@Override
	public Object getClientGuiElement( EntityPlayer player )
	{
		return new GuiEssentiaLevelEmitter( this, player );
	}

	@Override
	public Object getServerGuiElement( EntityPlayer player )
	{
		return new ContainerPartEssentiaLevelEmitter( this, player );
	}

	@Override
	public int isProvidingStrongPower()
	{
		return this.isPowering() ? 15 : 0;
	}

	@Override
	public int isProvidingWeakPower()
	{
		return this.isProvidingStrongPower();
	}

	@Override
	public void onStackChange( IItemList itemList, IAEStack fullStack, IAEStack diffStack, BaseActionSource source, StorageChannel channel )
	{
		if ( ( channel == StorageChannel.FLUIDS ) && ( diffStack != null ) )
		{
			// Get the gas for this aspect
			Fluid aspectGas = GaseousEssentia.getGasFromAspect( this.filterAspect );

			// Do the fluids match?
			if ( ( (IAEFluidStack) diffStack ).getFluid() == aspectGas )
			{
				// Set the current amount in the network
				this.setCurrentAmount( ( fullStack != null ? fullStack.getStackSize() : 0L ) );
			}

		}
	}

	@Override
	public void readFromNBT( NBTTagCompound data )
	{
		super.readFromNBT( data );

		this.filterAspect = Aspect.aspects.get( data.getString( "aspect" ) );

		this.mode = RedstoneMode.values()[data.getInteger( "mode" )];

		this.wantedAmount = data.getLong( "wantedAmount" );
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderInventory( IPartRenderHelper helper, RenderBlocks renderer )
	{
		helper.setTexture( BlockTextureManager.ESSENTIA_LEVEL_EMITTER.getTextures()[0] );
		helper.setBounds( 7.0F, 7.0F, 11.0F, 9.0F, 9.0F, 14.0F );
		helper.renderInventoryBox( renderer );

		helper.setTexture( BlockTextureManager.ESSENTIA_LEVEL_EMITTER.getTextures()[1] );
		helper.setBounds( 7.0F, 7.0F, 14.0F, 9.0F, 9.0F, 16.0F );
		helper.renderInventoryBox( renderer );

	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderStatic( int x, int y, int z, IPartRenderHelper helper, RenderBlocks renderer )
	{
		helper.setTexture( BlockTextureManager.ESSENTIA_LEVEL_EMITTER.getTextures()[0] );
		helper.setBounds( 7.0F, 7.0F, 11.0F, 9.0F, 9.0F, 14.0F );
		helper.renderBlock( x, y, z, renderer );
		
		if( this.isPowering() )
		{
			helper.setTexture( BlockTextureManager.ESSENTIA_LEVEL_EMITTER.getTextures()[2] );
			
			if ( this.isActive() )
			{
				Tessellator.instance.setBrightness( AEPartBase.ACTIVE_BRIGHTNESS );
			}
			
		}
		else
		{
			helper.setTexture( BlockTextureManager.ESSENTIA_LEVEL_EMITTER.getTextures()[1] );
		}
		
		helper.setBounds( 7.0F, 7.0F, 14.0F, 9.0F, 9.0F, 16.0F );
		helper.renderBlock( x, y, z, renderer );
	}

	@Override
	public void setAspect( int index, Aspect aspect, EntityPlayer player )
	{
		this.filterAspect = aspect;

		if ( this.watcher == null )
		{
			return;
		}

		this.watcher.clear();

		this.updateWatcher( this.watcher );

		new PacketAspectSlot( Lists.newArrayList( new Aspect[] { this.filterAspect } ) ).sendPacketToPlayer( player );
	}

	public void setWantedAmount( long wantedAmount, EntityPlayer player )
	{
		this.wantedAmount = wantedAmount;

		this.queryNetworkForCurrentAmount();

		if ( this.wantedAmount < 0L )
		{
			this.wantedAmount = 0L;
		} else if( this.wantedAmount > 9999999999L )
		{
			this.wantedAmount = 9999999999L;
		}
		
		new PacketEssentiaEmitter( this.wantedAmount, player ).sendPacketToPlayer( player );
		
		this.notifyNeighborsOfChange();
	}

	public void sendInformation( EntityPlayer player )
	{
		new PacketEssentiaEmitter( this.mode, player ).sendPacketToPlayer( player );

		new PacketEssentiaEmitter( this.wantedAmount, player ).sendPacketToPlayer( player );

		new PacketAspectSlot( Lists.newArrayList( new Aspect[] { this.filterAspect } ) ).sendPacketToPlayer( player );
	}

	public void toggleMode( EntityPlayer player )
	{
		switch ( this.mode )
		{
			case HIGH_SIGNAL:
				this.mode = RedstoneMode.LOW_SIGNAL;
				break;

			case LOW_SIGNAL:
				this.mode = RedstoneMode.HIGH_SIGNAL;
				break;

			case IGNORE:
			case SIGNAL_PULSE:
				break;
		}

		this.notifyNeighborsOfChange();

		new PacketEssentiaEmitter( this.mode, player ).sendPacketToPlayer( player );

	}

	@Override
	public void updateWatcher( IStackWatcher newWatcher )
	{
		this.watcher = newWatcher;

		if ( this.filterAspect != null )
		{
			this.watcher.add( EssentiaConversionHelper.createAEFluidStackInFluidUnits( GaseousEssentia.getGasFromAspect( this.filterAspect ), 1 ) );
		}
	}

	@Override
	public void writeToNBT( NBTTagCompound data )
	{
		super.writeToNBT( data );

		if ( this.filterAspect != null )
		{
			data.setString( "aspect", this.filterAspect.getTag() );
		}
		else
		{
			data.setString( "aspect", "" );
		}

		data.setInteger( "mode", this.mode.ordinal() );

		data.setLong( "wantedAmount", this.wantedAmount );
	}

	@Override
	public void onNeighborChanged()
	{	
		this.queryNetworkForCurrentAmount();
	}
	
	public boolean setFilteredAspectFromItemstack( EntityPlayer player, ItemStack itemStack )
	{
		Aspect itemAspect = EssentiaItemContainerHelper.getAspectInContainer( itemStack );

		if ( itemAspect != null )
		{
			this.filterAspect = itemAspect;
			
			if( !player.worldObj.isRemote )
			{
				this.sendInformation( player );
			}
			
			return true;
		}

		return false;
	}
	


	/**
	 * Determines how much power the part takes for just
	 * existing.
	 */
	@Override
	public double getIdlePowerUsage()
	{
		return AEPartEssentiaLevelEmitter.IDLE_POWER_DRAIN;
	}

}
