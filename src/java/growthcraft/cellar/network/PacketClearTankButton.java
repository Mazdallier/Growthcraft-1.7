package growthcraft.cellar.network;

import growthcraft.cellar.common.tileentity.TileEntityFermentBarrel;
import growthcraft.cellar.common.tileentity.TileEntityFruitPress;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class PacketClearTankButton extends AbstractPacketButton
{
	public PacketClearTankButton(){}

	public PacketClearTankButton(int x, int y, int z)
	{
		super(x, y, z);
	}

	@Override
	public void handleClientSide(EntityPlayer player)
	{

	}

	@Override
	public void handleServerSide(EntityPlayer player)
	{
		final World world = player.worldObj;
		final TileEntity te = world.getTileEntity(xCoord, yCoord, zCoord);

		if (te instanceof TileEntityFruitPress)
		{
			((TileEntityFruitPress)te).clearTank(0);
		}

		if (te instanceof TileEntityFermentBarrel)
		{
			((TileEntityFermentBarrel)te).clearTank(0);
		}
	}
}
