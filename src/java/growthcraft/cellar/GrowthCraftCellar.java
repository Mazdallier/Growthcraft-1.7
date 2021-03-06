package growthcraft.cellar;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import growthcraft.api.cellar.CellarRegistry;
import growthcraft.cellar.common.block.BlockBrewKettle;
import growthcraft.cellar.common.block.BlockFermentBarrel;
import growthcraft.cellar.common.block.BlockFruitPress;
import growthcraft.cellar.common.block.BlockFruitPresser;
import growthcraft.cellar.common.CommonProxy;
import growthcraft.cellar.common.item.ItemChievDummy;
import growthcraft.cellar.common.item.ItemYeast;
import growthcraft.cellar.common.potion.PotionCellar;
import growthcraft.cellar.common.tileentity.TileEntityBrewKettle;
import growthcraft.cellar.common.tileentity.TileEntityFermentBarrel;
import growthcraft.cellar.common.tileentity.TileEntityFruitPress;
import growthcraft.cellar.common.tileentity.TileEntityFruitPresser;
import growthcraft.cellar.common.village.ComponentVillageTavern;
import growthcraft.cellar.common.village.VillageHandlerCellar;
import growthcraft.cellar.creativetab.CreativeTabsCellar;
import growthcraft.cellar.event.ItemCraftedEventCellar;
import growthcraft.cellar.event.LivingUpdateEventCellar;
import growthcraft.cellar.handler.GuiHandlerCellar;
import growthcraft.cellar.network.PacketPipeline;
import growthcraft.cellar.util.YeastType;
import growthcraft.core.common.AchievementPageGrowthcraft;
import growthcraft.core.common.definition.BlockDefinition;
import growthcraft.core.common.definition.ItemDefinition;
import growthcraft.core.common.ModuleContainer;
import growthcraft.core.integration.NEI;
import growthcraft.core.util.MapGenHelper;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.VillagerRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.stats.Achievement;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

@Mod(
	modid = GrowthCraftCellar.MOD_ID,
	name = GrowthCraftCellar.MOD_NAME,
	version = GrowthCraftCellar.MOD_VERSION,
	dependencies = "required-after:Growthcraft"
)
public class GrowthCraftCellar
{
	public static final String MOD_ID = "Growthcraft|Cellar";
	public static final String MOD_NAME = "Growthcraft Cellar";
	public static final String MOD_VERSION = "@VERSION@";

	@Instance(MOD_ID)
	public static GrowthCraftCellar instance;

	public static CreativeTabs tab;

	public static BlockDefinition fruitPress;
	public static BlockDefinition fruitPresser;
	public static BlockDefinition brewKettle;
	public static BlockDefinition fermentBarrel;

	public static ItemDefinition yeast;

	public static Potion potionTipsy;

	// Constants
	public static final int BOTTLE_VOLUME = 333;
	public static final ItemStack EMPTY_BOTTLE = new ItemStack(Items.glass_bottle);

	// Achievments
	public static ItemDefinition chievItemDummy;

	public static Achievement craftBarrel;
	public static Achievement fermentBooze;
	public static Achievement getDrunk;

	public static final PacketPipeline packetPipeline = new PacketPipeline();

	private GrcCellarConfig config = new GrcCellarConfig();
	private ModuleContainer modules = new ModuleContainer();

	public static GrcCellarConfig getConfig()
	{
		return instance.config;
	}

	@EventHandler
	public void preload(FMLPreInitializationEvent event)
	{
		config.load(event.getModConfigurationDirectory(), "growthcraft/cellar.conf");

		//====================
		// INIT
		//====================
		tab = new CreativeTabsCellar("tabGrCCellar");
		fermentBarrel = new BlockDefinition(new BlockFermentBarrel());
		fruitPress    = new BlockDefinition(new BlockFruitPress());
		fruitPresser  = new BlockDefinition(new BlockFruitPresser());
		brewKettle    = new BlockDefinition(new BlockBrewKettle());

		yeast = new ItemDefinition(new ItemYeast());
		chievItemDummy = new ItemDefinition(new ItemChievDummy());

		register();
	}

	private void register()
	{
		//====================
		// REGISTRIES
		//====================
		GameRegistry.registerBlock(fruitPress.getBlock(), "grc.fruitPress");
		GameRegistry.registerBlock(fruitPresser.getBlock(), "grc.fruitPresser");
		GameRegistry.registerBlock(brewKettle.getBlock(), "grc.brewKettle");
		GameRegistry.registerBlock(fermentBarrel.getBlock(), "grc.fermentBarrel");

		GameRegistry.registerItem(yeast.getItem(), "grc.yeast");
		GameRegistry.registerItem(chievItemDummy.getItem(), "grc.chievItemDummy");

		GameRegistry.registerTileEntity(TileEntityFruitPress.class, "grc.tileentity.fruitPress");
		GameRegistry.registerTileEntity(TileEntityFruitPresser.class, "grc.tileentity.fruitPresser");
		GameRegistry.registerTileEntity(TileEntityBrewKettle.class, "grc.tileentity.brewKettle");
		GameRegistry.registerTileEntity(TileEntityFermentBarrel.class, "grc.tileentity.fermentBarrel");

		MapGenHelper.registerVillageStructure(ComponentVillageTavern.class, "grc.tavern");

		//====================
		// CRAFTING
		//====================
		GameRegistry.addRecipe(new ShapedOreRecipe(fruitPress.asStack(), "ABA", "CCC", "AAA", 'A', "plankWood", 'B', Blocks.piston,'C', "ingotIron"));
		GameRegistry.addRecipe(new ShapedOreRecipe(brewKettle.asStack(), "A", 'A', Items.cauldron));
		GameRegistry.addRecipe(new ShapedOreRecipe(fermentBarrel.asStack(), "AAA", "BBB", "AAA", 'B', "plankWood", 'A', "ingotIron"));

		//====================
		// POTION
		//====================
		registerPotions();
		potionTipsy = (new PotionCellar(config.potionTipsyID, false, 0)).setIconIndex(0, 0).setPotionName("grc.potion.tipsy");

		//====================
		// ACHIEVEMENTS
		//====================
		craftBarrel  = (new Achievement("grc.achievement.craftBarrel", "craftBarrel", -4, -4, fermentBarrel.getBlock(), (Achievement)null)).initIndependentStat().registerStat();
		fermentBooze = (new Achievement("grc.achievement.fermentBooze", "fermentBooze", -2, -4, Items.nether_wart, craftBarrel)).registerStat();
		getDrunk     = (new Achievement("grc.achievement.getDrunk", "getDrunk", 0, -4, chievItemDummy.asStack(), fermentBooze)).setSpecial().registerStat();

		AchievementPageGrowthcraft.chievMasterList.add(craftBarrel);
		AchievementPageGrowthcraft.chievMasterList.add(fermentBooze);
		AchievementPageGrowthcraft.chievMasterList.add(getDrunk);

		registerHeatSources();
		registerYeast();

		NEI.hideItem(fruitPresser.asStack());
		NEI.hideItem(chievItemDummy.asStack());
	}

	private void registerPotions()
	{
		Potion[] potionTypes = null;

		for (Field f : Potion.class.getDeclaredFields())
		{
			f.setAccessible(true);
			try
			{
				if (f.getName().equals("potionTypes") || f.getName().equals("field_76425_a"))
				{
					final Field modfield = Field.class.getDeclaredField("modifiers");
					modfield.setAccessible(true);
					modfield.setInt(f, f.getModifiers() & ~Modifier.FINAL);

					potionTypes = (Potion[])f.get(null);
					final Potion[] newPotionTypes = new Potion[256];
					System.arraycopy(potionTypes, 0, newPotionTypes, 0, potionTypes.length);
					f.set(null, newPotionTypes);
				}
			}
			catch (Exception e)
			{
				System.err.println("Severe error, please report this to the mod author:");
				System.err.println(e);
			}
		}
	}

	private void registerHeatSources()
	{
		CellarRegistry.instance().heatSource().addHeatSource(Blocks.fire, -1, 1.0f);
		CellarRegistry.instance().heatSource().addHeatSource(Blocks.lava, -1, 0.5f);
		CellarRegistry.instance().heatSource().addHeatSource(Blocks.flowing_lava, -1, 0.5f);
	}

	private void registerOres()
	{
		OreDictionary.registerOre("materialYeast", yeast.getItem());
	}

	private void registerYeast()
	{
		CellarRegistry.instance().fermenting().addYeastToBiomeType(YeastType.LAGER.asStack(1), BiomeDictionary.Type.COLD);
		CellarRegistry.instance().fermenting().addYeastToBiomeType(YeastType.ETHEREAL.asStack(1), BiomeDictionary.Type.MAGICAL);
		CellarRegistry.instance().fermenting().addYeastToBiomeType(YeastType.ORIGIN.asStack(1), BiomeDictionary.Type.MUSHROOM);
	}

	@EventHandler
	public void load(FMLInitializationEvent event)
	{
		registerOres();
		CommonProxy.instance.initRenders();

		packetPipeline.initialise();
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandlerCellar());

		VillagerRegistry.instance().registerVillageCreationHandler(new VillageHandlerCellar());
		CommonProxy.instance.registerVillagerSkin();

		new growthcraft.cellar.integration.Waila();
	}

	@EventHandler
	public void postload(FMLPostInitializationEvent event)
	{
		packetPipeline.postInitialise();
		MinecraftForge.EVENT_BUS.register(new ItemCraftedEventCellar());
		MinecraftForge.EVENT_BUS.register(new LivingUpdateEventCellar());
		if (config.enableThaumcraftIntegration) new growthcraft.cellar.integration.ThaumcraftModule().init();
	}
}
