package schmoller.tubes.logic;

import codechicken.core.data.MCDataInput;
import codechicken.core.data.MCDataOutput;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import schmoller.tubes.ITube;
import schmoller.tubes.ITubeConnectable;
import schmoller.tubes.ModTubes;
import schmoller.tubes.TubeHelper;
import schmoller.tubes.TubeItem;

public class CompressorTubeLogic extends TubeLogic implements IInventory
{
	private ITube mTube;
	private TubeItem mCurrent;
	private ItemStack mTarget;
	
	public CompressorTubeLogic(ITube tube)
	{
		mTube = tube;
		mTarget = new ItemStack(0, 64, 0);
		mCurrent = null;
	}
	
	@Override
	public boolean hasCustomRouting()
	{
		return true;
	}
	
	@Override
	public boolean canItemEnter( TubeItem item, int side )
	{
		if(mCurrent != null)
			return (item.item.isItemEqual(mCurrent.item) && ItemStack.areItemStackTagsEqual(item.item, mCurrent.item));
		
		return true;
	}
	
	@Override
	public void onLoad( NBTTagCompound root )
	{
		NBTTagCompound target = (NBTTagCompound)root.getTag("Target");
		mTarget = new ItemStack(0,0,0);
		mTarget.readFromNBT(target);
		
		
		if(root.hasKey("Current"))
		{
			NBTTagCompound current = (NBTTagCompound)root.getTag("Current");
			mCurrent = TubeItem.readFromNBT(current);
		}
	}
	
	@Override
	public void onSave( NBTTagCompound root )
	{
		NBTTagCompound target = new NBTTagCompound();
		mTarget.writeToNBT(target);
		root.setTag("Target", target);
		
		if(mCurrent != null)
		{
			NBTTagCompound current = new NBTTagCompound();
			mCurrent.writeToNBT(current);
			root.setTag("Current", current);
		}
	}
	
	@Override
	public void readDesc( MCDataInput input )
	{
		if(input.readBoolean())
			mCurrent = TubeItem.read(input);
		else
			mCurrent = null;
	}
	
	@Override
	public void writeDesc( MCDataOutput output )
	{
		if(mCurrent != null)
		{
			output.writeBoolean(true);
			mCurrent.write(output);
		}
		else
			output.writeBoolean(false);
	}
	
	@Override
	public int onDetermineDestination( TubeItem item )
	{
		if(mCurrent != null)
		{
			if(!item.item.isItemEqual(mCurrent.item) || !ItemStack.areItemStackTagsEqual(item.item, mCurrent.item))
				return item.direction ^ 1;
			
			int amt = Math.min(item.item.stackSize, mTarget.stackSize - mCurrent.item.stackSize);
			mCurrent.item.stackSize += amt;
			item.item.stackSize -= amt;
			
			if(mCurrent.item.stackSize == mTarget.stackSize)
			{
				mCurrent.updated = true;
				mTube.addItem(mCurrent, true);
				mCurrent = null;
				
				if(item.item.stackSize > 0)
					mCurrent = item.clone();
			}
			
			return -2;
		}
		else if(mTarget.itemID == 0 || (item.item.isItemEqual(mTarget) && ItemStack.areItemStackTagsEqual(item.item, mTarget)))
		{
			if(item.item.stackSize < mTarget.stackSize)
			{
				mCurrent = item.clone();
				return -2;
			}
		}
		
		int conns = mTube.getConnections();
		int count = 0;
		int dir = 0;
		
		conns -= (conns & (1 << (item.direction ^ 1)));
		
		for(int i = 0; i < 6; ++i)
		{
			if((conns & (1 << i)) != 0)
			{
				++count;
				dir = i;
			}
		}
		
		if(count > 1)
			dir = TubeHelper.findNextDirection(mTube.world(), mTube.x(), mTube.y(), mTube.z(), item);
		
		return dir;
	}
	
	@Override
	public boolean canConnectTo( ITubeConnectable con )
	{
		if(con instanceof ITube)
			return !(((ITube)con).getLogic() instanceof CompressorTubeLogic);

		return true;
	}
	
	public ItemStack getTargetType()
	{
		return mTarget;
	}
	
	public void setTargetType(ItemStack item)
	{
		if(item == null)
			mTarget = new ItemStack(0, 64, 0);
		else
			mTarget = item;
	}

	@Override
	public int getSizeInventory()
	{
		return 1;
	}

	@Override
	public ItemStack getStackInSlot( int i )
	{
		if(mCurrent != null)
			return mCurrent.item;
		return null;
	}

	@Override
	public ItemStack decrStackSize( int slot, int count )
	{
		if(mCurrent == null)
			return null;
		
        ItemStack itemstack;

        if (mCurrent.item.stackSize <= count)
        {
            itemstack = mCurrent.item;
            mCurrent = null;
        }
        else
        {
            itemstack = mCurrent.item.splitStack(count);

            if (mCurrent.item.stackSize == 0)
            	mCurrent = null;
        }
        
        onInventoryChanged();
        return itemstack;
	}

	@Override
	public ItemStack getStackInSlotOnClosing( int i )
	{
		if(mCurrent == null)
			return null;
		
		ItemStack item = mCurrent.item;
		mCurrent = null;
		
		return item;
	}

	@Override
	public void setInventorySlotContents( int i, ItemStack item )
	{
		if(item == null)
		{
			mCurrent = null;
			return;
		}
		
		if(mCurrent == null)
		{
			mCurrent = new TubeItem(item);
			mCurrent.direction = 6;
			mCurrent.progress = 0.5f;
		}
		else
			mCurrent.item = item;
		
		if(mCurrent.item.stackSize >= mTarget.stackSize)
		{
			mTube.addItem(mCurrent, true);
			mCurrent = null;
		}
	}

	@Override
	public String getInvName()
	{
		return "container.inventory";
	}

	@Override
	public boolean isInvNameLocalized()
	{
		return false;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return mTarget.stackSize;
	}

	@Override
	public void onInventoryChanged()
	{
		
	}

	@Override
	public boolean isUseableByPlayer( EntityPlayer player )
	{
		return player.getDistanceSq(mTube.x(), mTube.y(), mTube.z()) <= 25;
	}

	@Override
	public void openChest() {}

	@Override
	public void closeChest() {}

	@Override
	public boolean isStackValidForSlot( int i, ItemStack item )
	{
		return (mTarget.itemID == 0 || (item.isItemEqual(mTarget) && ItemStack.areItemStackTagsEqual(item, mTarget)));
	}
	
	@Override
	public boolean onActivate( EntityPlayer player )
	{
		player.openGui(ModTubes.instance, ModTubes.GUI_COMPRESSOR_TUBE, mTube.world(), mTube.x(), mTube.y(), mTube.z());
		
		return true;
	}
}
