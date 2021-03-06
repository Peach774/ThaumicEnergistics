package thaumicenergistics.network.packet;

import io.netty.buffer.ByteBuf;
import java.util.Iterator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import thaumicenergistics.gui.GuiArcaneCraftingTerminal;
import thaumicenergistics.network.AbstractPacket;
import appeng.api.AEApi;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;

public class PacketClientArcaneCraftingTerminal
	extends AbstractPacket
{
	private static final byte MODE_RECEIVE_CHANGE = 0;
	private static final byte MODE_RECEIVE_FULL_LIST = 1;
	private static final byte MODE_RECEIVE_PLAYER_HOLDING = 2;

	private IAEItemStack changedStack;
	
	private IItemList<IAEItemStack> fullList;
	
	private boolean isHeldEmpty;

	public PacketClientArcaneCraftingTerminal()
	{
	}

	/**
	 * Creates a packet with a changed network stack amount
	 * 
	 * @param player
	 * @param change
	 */
	public PacketClientArcaneCraftingTerminal createChangeUpdate( EntityPlayer player, IAEItemStack change )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketClientArcaneCraftingTerminal.MODE_RECEIVE_CHANGE;

		// Set the change
		this.changedStack = change;
		
		return this;
	}

	/**
	 * Creates a packet with the full list of items in the AE network.
	 * Only send in response to a request.
	 * 
	 * @param player
	 * @param fullList
	 */
	public PacketClientArcaneCraftingTerminal createFullListUpdate( EntityPlayer player, IItemList<IAEItemStack> fullList )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketClientArcaneCraftingTerminal.MODE_RECEIVE_FULL_LIST;

		// Enable compression
		this.useCompression = true;

		// Set the full list
		this.fullList = fullList;
		
		return this;
	}

	/**
	 * Creates a packet with an update to what itemstack the player is holding.
	 * @param player
	 * @param heldItem
	 * @return
	 */
	public PacketClientArcaneCraftingTerminal createPlayerHoldingUpdate( EntityPlayer player, IAEItemStack heldItem )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketClientArcaneCraftingTerminal.MODE_RECEIVE_PLAYER_HOLDING;
		
		// Set the held item
		this.changedStack = heldItem;
		
		// Is the player holding anything?
		this.isHeldEmpty = ( heldItem == null );
		
		return this;
	}

	@Override
	public void execute()
	{
		// Ensure we have a player
		if( ( this.player != null ) )
		{
			// Get the current screen being displayed to the user
			Gui gui = Minecraft.getMinecraft().currentScreen;

			// Is that screen the gui for the ACT?
			if( gui instanceof GuiArcaneCraftingTerminal )
			{
				switch ( this.mode )
				{
					case PacketClientArcaneCraftingTerminal.MODE_RECEIVE_FULL_LIST:
						// Set the item list
						( (GuiArcaneCraftingTerminal)gui ).onReceiveFullList( this.fullList );
						break;

					case PacketClientArcaneCraftingTerminal.MODE_RECEIVE_CHANGE:
						// Update the item list
						( (GuiArcaneCraftingTerminal)gui ).onReceiveChange( this.changedStack );
						break;
						
					case PacketClientArcaneCraftingTerminal.MODE_RECEIVE_PLAYER_HOLDING:
						// Set the held item
						( (GuiArcaneCraftingTerminal)gui ).onPlayerHeldReceived( this.changedStack );
				}
			}
		}
	}

	@Override
	public void readData( ByteBuf stream )
	{

		switch ( this.mode )
		{
			case PacketClientArcaneCraftingTerminal.MODE_RECEIVE_FULL_LIST:
				// Create a new list
				this.fullList = AEApi.instance().storage().createItemList();

				// Read how many items there are
				int count = stream.readInt();

				for( int i = 0; i < count; i++ )
				{
					// Also ensure there are bytes to read
					if( stream.readableBytes() <= 0 )
					{
						break;
					}

					// Read the itemstack
					IAEItemStack itemStack = AbstractPacket.readAEItemStack( stream );

					// Ensure it is not null
					if( itemStack != null )
					{
						// Add to the list
						this.fullList.add( itemStack );
					}
				}
				break;

			case PacketClientArcaneCraftingTerminal.MODE_RECEIVE_CHANGE:
				// Read the change amount
				int changeAmount = stream.readInt();
				
				// Read the item
				this.changedStack = AbstractPacket.readAEItemStack( stream );
				
				// Adjust it's size
				this.changedStack.setStackSize( changeAmount );
				
				break;
				
			case PacketClientArcaneCraftingTerminal.MODE_RECEIVE_PLAYER_HOLDING:
				// Read if the itemstack is empty
				this.isHeldEmpty = stream.readBoolean();
				
				// Is it not empty?
				if( !this.isHeldEmpty )
				{
					this.changedStack = AbstractPacket.readAEItemStack( stream );
				}
				else
				{
					this.changedStack = null;
				}
				break;
		}
	}

	@Override
	public void writeData( ByteBuf stream )
	{

		switch ( this.mode )
		{
			case PacketClientArcaneCraftingTerminal.MODE_RECEIVE_FULL_LIST:
				// Write how many items there are
				stream.writeInt( this.fullList.size() );

				// Get the iterator
				Iterator<IAEItemStack> listIterator = this.fullList.iterator();

				// Write each item
				while( listIterator.hasNext() )
				{
					AbstractPacket.writeAEItemStack( listIterator.next(), stream );
				}
				break;

			case PacketClientArcaneCraftingTerminal.MODE_RECEIVE_CHANGE:
				// Write the change amount
				stream.writeInt( (int)this.changedStack.getStackSize() );
				
				// Write the change
				AbstractPacket.writeAEItemStack( this.changedStack, stream );
				break;
				
			case PacketClientArcaneCraftingTerminal.MODE_RECEIVE_PLAYER_HOLDING:
				// Write if the held item is empty
				stream.writeBoolean( this.isHeldEmpty );
				
				// Is it not empty?
				if( !this.isHeldEmpty )
				{
					// Write the stack
					AbstractPacket.writeAEItemStack( this.changedStack, stream );
				}
				
				break;
		}

	}

	/**
	 * Invalid, can only be sent to clients
	 */
	@Override
	public void sendPacketToServer()
	{
	}

}
