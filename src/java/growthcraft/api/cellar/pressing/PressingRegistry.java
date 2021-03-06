package growthcraft.api.cellar.pressing;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import growthcraft.api.cellar.common.Residue;
import growthcraft.api.cellar.util.FluidUtils;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class PressingRegistry
{
	// because damage is almost never -1
	private final int NO_META = -1;
	private Map<List, PressingResult> pressingList = new HashMap<List, PressingResult>();

	public Map<List, PressingResult> getPressingList()
	{
		return pressingList;
	}

	/**
	 * addPressing()
	 *
	 * Example Usage:
	 * CellarRegistry.instance().pressing().addPressing(Item.appleRed, appleCider_booze[0], 20, 37, 0.3F);
	 *
	 * @param raw     - The source/input Block/Item/ID
	 * @param meta    - The metadata of @param raw
	 * @param fluid   - The resulting fluid.
	 * @param time    - The time needed for the item/block to be pressed.
	 * @param amount  - The amount of booze the item/block produces.
	 * @param residue - The amount of residue this will produce.
	 */
	public void addPressing(Item raw, int meta, Fluid fluid, int time, int amount, Residue residue)
	{
		this.pressingList.put(Arrays.asList(raw, meta), new PressingResult(new FluidStack(fluid, amount), time, residue));
	}

	public void addPressing(Block raw, int meta, Fluid fluid, int time, int amount, Residue residue)
	{
		this.addPressing(Item.getItemFromBlock(raw), meta, fluid, time, amount, residue);
	}

	public void addPressing(Block raw, int meta, String fluid, int time, int amount, Residue residue)
	{
		this.addPressing(Item.getItemFromBlock(raw), meta, fluid, time, amount, residue);
	}

	public void addPressing(Item raw, int meta, String fluid, int time, int amount, Residue residue)
	{
		if (FluidUtils.doesFluidExist(fluid))
		{
			this.addPressing(raw, meta, FluidRegistry.getFluid(fluid), time, amount, residue);
		}
	}

	/**
	 * addPressing()
	 *
	 * Example Usage:
	 * CellarRegistry.instance().pressing().addPressing(Item.appleRed, appleCider_booze[0], 20, 37, 0.3F);
	 *
	 * @param raw     - The source/input Block/Item/ID
	 * @param fluid   - The resulting fluid.
	 * @param time    - The time needed for the item/block to be pressed.
	 * @param amount  - The amount of booze the item/block produces.
	 * @param residue - The amount of residue this will produce.
	 */
	public void addPressing(Item raw, Fluid fluid, int time, int amount, Residue residue)
	{
		addPressing(raw, NO_META, fluid, time, amount, residue);
	}

	public void addPressing(Item raw, String fluid, int time, int amount, Residue residue)
	{
		addPressing(raw, NO_META, fluid, time, amount, residue);
	}

	public void addPressing(Block raw, Fluid fluid, int time, int amount, Residue residue)
	{
		addPressing(Item.getItemFromBlock(raw), NO_META, fluid, time, amount, residue);
	}

	public void addPressing(Block raw, String fluid, int time, int amount, Residue residue)
	{
		addPressing(Item.getItemFromBlock(raw), NO_META, fluid, time, amount, residue);
	}

	public boolean isPressingRecipe(ItemStack itemstack)
	{
		return this.getPressingResults(itemstack)!= null;
	}

	public PressingResult getPressingResults(ItemStack itemstack)
	{
		if (itemstack == null) return null;

		final PressingResult ret = pressingList.get(Arrays.asList(itemstack.getItem(), itemstack.getItemDamage()));
		if (ret != null) return ret;

		return pressingList.get(Arrays.asList(itemstack.getItem(), NO_META));
	}

	public FluidStack getPressingFluidStack(ItemStack itemstack)
	{
		final PressingResult pressresults = this.getPressingResults(itemstack);
		if (pressresults == null) return null;

		return pressresults.asFluidStack(1);
	}

	public int getPressingTime(ItemStack itemstack)
	{
		final PressingResult pressresults = this.getPressingResults(itemstack);
		if (pressresults == null) return 0;

		return pressresults.time;
	}

	public int getPressingAmount(ItemStack itemstack)
	{
		final PressingResult pressresults = this.getPressingResults(itemstack);
		if (pressresults == null) return 0;

		return pressresults.getAmount();
	}

	public Residue getPressingResidue(ItemStack itemstack)
	{
		final PressingResult pressresults = this.getPressingResults(itemstack);
		if (pressresults == null) return null;

		return pressresults.residue;
	}
}
