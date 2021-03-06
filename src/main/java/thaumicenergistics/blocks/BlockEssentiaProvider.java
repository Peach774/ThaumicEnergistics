package thaumicenergistics.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import thaumicenergistics.registries.BlockEnum;
import thaumicenergistics.registries.Renderers;
import thaumicenergistics.texture.BlockTextureManager;
import thaumicenergistics.tileentities.TileEssentiaProvider;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Block definition of the essentia provider.
 * 
 * @author Nividica
 * 
 */
public class BlockEssentiaProvider
	extends BlockBaseProvider
{

	public BlockEssentiaProvider()
	{
		// Call super with material machine (iron) 
		super( Material.iron );

		// Basic hardness
		this.setHardness( 1.0f );

		// Sound of metal
		this.setStepSound( Block.soundTypeMetal );
	}

	@Override
	public TileEntity createNewTileEntity( World world, int metaData )
	{
		// Create a new provider tile, passing the side to attach to
		TileEssentiaProvider tile = new TileEssentiaProvider();
		
		// Setup the essentia provider
		tile.setupProvider( metaData );
		
		// Return the tile
		return tile;
	}

	@Override
	public void onNeighborBlockChange( World world, int x, int y, int z, Block neighbor )
	{
		// Inform our tile entity a neighbor has changed
		( (TileEssentiaProvider)world.getTileEntity( x, y, z ) ).checkGridConnectionColor();
	}

	@Override
	public int getRenderType()
	{
		// Provide our custom ID
		return Renderers.EssentiaProviderRenderID;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon( int side, int metaData )
	{
		return BlockTextureManager.ESSENTIA_PROVIDER.getTexture();
	}

	@Override
	public String getUnlocalizedName()
	{
		return BlockEnum.ESSENTIA_PROVIDER.getUnlocalizedName();
	}

}
