package thaumicenergistics.aspect;

import net.minecraft.nbt.NBTTagCompound;
import thaumcraft.api.aspects.Aspect;

/**
 * Stores an aspect and an amount.
 * @author Nividica
 *
 */
public class AspectStack
{
	/**
	 * Creates an aspect stack from a NBT compound tag.
	 * @param nbt Tag to load from
	 * @return Created stack, or null.
	 */
	public static AspectStack loadAspectStackFromNBT( NBTTagCompound nbt )
	{
		// Attempt to get the aspect
		Aspect aspect = Aspect.aspects.get( nbt.getString( "AspectTag" ) );

		// Did we get an aspect?
		if ( aspect == null )
		{
			return null;
		}

		// Load the amount
		long amount = nbt.getLong( "Amount" );

		// Return a newly created stack.
		return new AspectStack( aspect, amount );
	}

	/**
	 * The aspect this stack contains.
	 */
	public Aspect aspect;

	/**
	 * The amount this stack contains
	 */
	public long amount;

	/**
	 * Creates an empty stack
	 */
	public AspectStack()
	{
		this.aspect = null;
		this.amount = 0;
	}

	/**
	 * Creates a stack using the specified aspect and amount.
	 * @param aspect What aspect this stack will have.
	 * @param amount How much this stack will have.
	 */
	public AspectStack( Aspect aspect, long amount )
	{
		this.aspect = aspect;

		this.amount = amount;
	}

	/**
	 * Sets this stacks values to match that of the specified stack.
	 * @param source
	 */
	public AspectStack( AspectStack source )
	{
		this.aspect = source.aspect;

		this.amount = source.amount;
	}

	/**
	 * Creates a copy of this stack and returns it.
	 * @return Copy of the stack.
	 */
	public AspectStack copy()
	{
		return new AspectStack( this );
	}
	
	/**
	 * The chat color associated with this aspect.
	 * @return Chat prefix string, or empty string if no aspect.
	 */
	public String getChatColor()
	{
		// Do we have an aspect?
		if( this.aspect != null )
		{
			return this.aspect.getChatcolor();
		}
		
		return "";
	}
	
	/**
	 * Gets the Thaumcraft tag for the aspect
	 * @return Thaumcraft tag, or empty string if no aspect.
	 */
	public String getTag()
	{
		// Do we have an aspect?
		if( this.aspect != null )
		{
			return this.aspect.getTag();
		}
		
		return "";
	}
	
	/**
	 * Gets the display name for the aspect.
	 * @return Aspect name, or empty string if no aspect.
	 */
	public String getName()
	{
		// Do we have an aspect?
		if( this.aspect != null )
		{
			return this.aspect.getName();
		}
		
		return "";
	}

	/**
	 * Writes this aspect stack to the specified NBT tag
	 * @param nbt The tag to write to
	 * @return The nbt tag.
	 */
	public NBTTagCompound writeToNBT( NBTTagCompound nbt )
	{
		// Do we have an aspect?
		if( this.aspect != null )
		{
			nbt.setString( "AspectTag", this.aspect.getTag() );

			nbt.setLong( "Amount", this.amount );
		}

		return nbt;
	}
}
