package growthcraft.bees.common.block;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class BlockThaumcraftBeeBox extends BlockBeeBox
{
	public static class ThaumcraftBeeBoxType
	{
		public static final int GREATWOOD = 0;
		public static final int SILVERWOOD = 1;

		private ThaumcraftBeeBoxType() {}
	}

	public BlockThaumcraftBeeBox()
	{
		super();
		this.setBlockName("grc.thaumcraftBeeBox");
	}

	public void getSubBlocks(Item block, CreativeTabs tab, List list)
	{
		list.add(new ItemStack(block, 1, ThaumcraftBeeBoxType.GREATWOOD));
		list.add(new ItemStack(block, 1, ThaumcraftBeeBoxType.SILVERWOOD));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg)
	{
		this.icons = new IIcon[2 * 4];
		registerBeeBoxIcons(reg, "greatwood", ThaumcraftBeeBoxType.GREATWOOD);
		registerBeeBoxIcons(reg, "silverwood", ThaumcraftBeeBoxType.SILVERWOOD);
	}
}
