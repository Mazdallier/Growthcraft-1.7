package growthcraft.cellar.common.item;

import java.util.List;
import java.util.Set;

import growthcraft.api.cellar.CellarRegistry;
import growthcraft.cellar.GrowthCraftCellar;
import growthcraft.core.util.ItemUtils;
import growthcraft.core.util.UnitFormatter;
import growthcraft.core.Utils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;

public class ItemBoozeBottle extends ItemFood
{
	private Fluid[] boozes;

	private boolean canCauseTipsy;
	private float   tipsyChance;
	private int     tipsyTime;

	private boolean hasPotionEffect;
	private int     potionAmount;
	private int[]   potionID;
	private int[]   potionTime;

	@SideOnly(Side.CLIENT)
	private IIcon bottle;
	@SideOnly(Side.CLIENT)
	private IIcon contents;
	@SideOnly(Side.CLIENT)
	private IIcon liquid;

	private int color = 0xFFFFFF;

	public ItemBoozeBottle(int nut, float sat, Fluid[] boozeAry)
	{
		super(nut, sat, false);
		this.setAlwaysEdible();
		this.setMaxStackSize(4);
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
		this.setContainerItem(Items.glass_bottle);
		this.setCreativeTab(GrowthCraftCellar.tab);

		this.boozes = boozeAry;
	}

	public ItemBoozeBottle setColor(int kolor)
	{
		this.color = kolor;
		return this;
	}

	public ItemBoozeBottle setTipsy(float chance, int time)
	{
		this.canCauseTipsy = true;
		this.tipsyChance = MathHelper.clamp_float(chance, 0.1F, 1.0F);
		this.tipsyTime = time;
		return this;
	}

	public ItemBoozeBottle setPotionEffects(int[] ids, int[] times)
	{
		if (ids.length == times.length)
		{
			this.hasPotionEffect   = true;
			this.potionID     = ids;
			this.potionTime   = times;
			this.potionAmount = ids.length;
		}
		else
		{
			throw new IllegalArgumentException("[GrowthCraft Cellar] Error at creating a new bottle of booze. Check array lengths of potion effects.");
		}
		return this;
	}

	public Fluid[] getBoozeArray()
	{
		return this.boozes;
	}

	public Fluid getBooze(int i)
	{
		if (i >= boozes.length)
		{
			return boozes[0];
		}
		else
		{
			return boozes[i];
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5)
	{
		if (stack.getItemDamage() >= getBoozeArray().length)
		{
			stack.setItemDamage(0);
		}
	}

	/************
	 * ON USE
	 ************/

	// for lack of a better name, this method is called by the server side
	// when the item is eaten
	public void onEaten_server(ItemStack stack, World world, EntityPlayer player)
	{
		if (Utils.isIntegerInRange(stack.getItemDamage(), 1, 3))
		{
			/*int amplifier = 0;

			if (player.isPotionActive(Potion.confusion))
			{
				amplifier = player.getActivePotionEffect(Potion.confusion).getAmplifier();
				amplifier += 1;

				if (amplifier >= 6)
				{
					amplifier = 6;
				}
			}

			float chance = this.nauseaChance + (((this.nauseaChance / 4.0F) * (float)amplifier));
			int time = this.nauseaTime + (((this.nauseaTime / 4) * amplifier));

			if (world.rand.nextFloat() < chance && this.nauseaBool)
			{
				player.addPotionEffect(new PotionEffect(Potion.confusion.id, time, amplifier));

				if (amplifier >= 3)
				{
					player.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, time, amplifier - 3));
					player.addPotionEffect(new PotionEffect(Potion.digSlowdown.id, time, amplifier - 3));
				}
			}*/

			if (this.canCauseTipsy)
			{
				if (world.rand.nextFloat() < this.tipsyChance)
				{
					int amplifier = 0;
					int time = 1200;
					if (player.isPotionActive(GrowthCraftCellar.potionTipsy))
					{
						amplifier = player.getActivePotionEffect(GrowthCraftCellar.potionTipsy).getAmplifier() + 1;
						if (amplifier > 4)
						{
							amplifier = 4;
						}
					}

					switch (amplifier)
					{
						case 1: time = 3000; break;
						case 2: time = 6750; break;
						case 3: time = 12000; break;
						case 4: time = 24000; break;
						default:
							break;
					}

					player.addPotionEffect(new PotionEffect(GrowthCraftCellar.potionTipsy.id, time, amplifier));

					if (amplifier >= 4)
					{
						player.addStat(GrowthCraftCellar.getDrunk, 1);
					}
				}
			}

			if (this.hasPotionEffect)
			{
				for (int loop = 0; loop < this.potionAmount; ++loop)
				{
					this.addPotionEffect(stack, player, this.potionID[loop], this.potionTime[loop]);
				}
			}
		}
	}

	@Override
	public ItemStack onEaten(ItemStack stack, World world, EntityPlayer player)
	{
		if (!player.capabilities.isCreativeMode)
		{
			if (!world.isRemote)
			{
				final ItemStack result = ItemUtils.consumeStack(stack.splitStack(1));
				ItemUtils.addStackToPlayer(result, player, world, false);
			}
		}

		player.getFoodStats().func_151686_a(this, stack);
		world.playSoundAtEntity(player, "random.burp", 0.5F, world.rand.nextFloat() * 0.1F + 0.9F);
		this.onFoodEaten(stack, world, player);

		if (!world.isRemote)
		{
			onEaten_server(stack, world, player);
		}

		return stack.stackSize <= 0 ? null : stack;
	}

	protected PotionEffect makePotionEffect(ItemStack stack, int potnID, int potnTime)
	{
		final Fluid booze = getBooze(stack.getItemDamage());
		final Set<String> tags = CellarRegistry.instance().booze().getTags(booze);

		if (tags != null)
		{
			if (tags.contains("fermented"))
			{
				int time = potnTime;
				int lvl = 0;
				if (tags.contains("extended"))
				{
					time *= 2.67F;
				}

				if (tags.contains("potent"))
				{
					time /= 2;
					lvl += 1;
				}

				return new PotionEffect(potnID, time, lvl);
			}
		}
		return null;
	}

	protected void addPotionEffect(ItemStack stack, EntityPlayer player, int potnID, int potnTime)
	{
		final PotionEffect potFx = makePotionEffect(stack, potnID, potnTime);
		if (potFx != null)
		{
			player.addPotionEffect(potFx);
		}
	}

	/************
	 * TOOLTIP
	 ************/
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool)
	{
		writeModifierTooltip(stack, player, list, bool);

		if (Utils.isIntegerInRange(stack.getItemDamage(), 1, 3))
		{
			if (this.canCauseTipsy)
			{
				writeNauseaTooltip(stack, player, list, bool, this.tipsyChance, this.tipsyTime);
			}

			if (this.hasPotionEffect)
			{
				for (int loop = 0; loop < this.potionAmount; ++loop)
				{
					writePotionTooltip(stack, player, list, bool, this.potionID[loop], this.potionTime[loop]);
				}
			}
		}
	}

	protected void writeModifierTooltip(ItemStack stack, EntityPlayer player, List list, boolean bool)
	{
		final String s = UnitFormatter.fluidModifier(this.getBooze(stack.getItemDamage()));
		if (s != null) list.add(s);
	}

	@SuppressWarnings("rawtypes")
	protected void writePotionTooltip(ItemStack stack, EntityPlayer player, List list, boolean bool, int potnID, int potnTime)
	{
		final PotionEffect pe = makePotionEffect(stack, potnID, potnTime);

		String s = StatCollector.translateToLocal(pe.getEffectName()).trim();
		if (pe.getAmplifier() > 0)
		{
			s = s + " " + StatCollector.translateToLocal("potion.potency." + pe.getAmplifier()).trim();
		}

		if (pe.getDuration() > 20)
		{
			s = s + " (" + Potion.getDurationString(pe) + ")";
		}
		list.add(EnumChatFormatting.GRAY + s);
	}

	@SuppressWarnings("rawtypes")
	protected void writeNauseaTooltip(ItemStack stack, EntityPlayer player, List list, boolean bool, float nauseaChance, int nauseaTime)
	{
		final PotionEffect nausea = new PotionEffect(Potion.confusion.id, nauseaTime, 0);
		String n = "";
		final String p = StatCollector.translateToLocalFormatted("grc.cellar.format.tipsy_chance", Math.round(nauseaChance * 100));

		if (nausea.getDuration() > 20)
		{
			n = n + "(" + Potion.getDurationString(nausea) + ")";
		}
		list.add(EnumChatFormatting.GRAY + p + " " + n);
	}

	/************
	 * STUFF
	 ************/
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister reg)
	{
		this.bottle = reg.registerIcon("grccellar:booze");
		this.contents = reg.registerIcon("grccellar:booze_contents");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamageForRenderPass(int par1, int pass)
	{
		return pass == 0 ? this.contents : this.bottle;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int pass)
	{
		return pass == 0 ? this.color : 0xFFFFFF;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean requiresMultipleRenderPasses()
	{
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack, int pass)
	{
		if (Utils.isIntegerInRange(stack.getItemDamage(), 1, 3))
		{
			return true;
		}
		return false;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		return super.getUnlocalizedName();
	}

	@Override
	public int getMaxItemUseDuration(ItemStack par1ItemStack)
	{
		return 32;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack par1ItemStack)
	{
		return EnumAction.drink;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
		return stack;
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack)
	{
		return StatCollector.translateToLocal(CellarRegistry.instance().booze().getBoozeName(getBoozeArray()));
	}

	@SuppressWarnings("rawtypes")
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list)
	{
		for (int i = 0; i < getBoozeArray().length; i++)
		{
			list.add(new ItemStack(item, 1, i));
		}
	}
}
