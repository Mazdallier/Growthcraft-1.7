package growthcraft.hops;

import growthcraft.api.cellar.booze.Booze;
import growthcraft.api.cellar.CellarRegistry;
import growthcraft.api.cellar.common.Residue;
import growthcraft.api.core.CoreRegistry;
import growthcraft.cellar.common.definition.BlockBoozeDefinition;
import growthcraft.cellar.common.definition.ItemBucketBoozeDefinition;
import growthcraft.cellar.common.item.ItemBoozeBottle;
import growthcraft.cellar.common.item.ItemBoozeBucketDEPRECATED;
import growthcraft.cellar.GrowthCraftCellar;
import growthcraft.cellar.util.BoozeRegistryHelper;
import growthcraft.core.common.definition.BlockTypeDefinition;
import growthcraft.core.common.definition.ItemDefinition;
import growthcraft.core.common.ModuleContainer;
import growthcraft.core.GrowthCraftCore;
import growthcraft.core.integration.NEI;
import growthcraft.core.util.MapGenHelper;
import growthcraft.hops.common.block.BlockHops;
import growthcraft.hops.common.CommonProxy;
import growthcraft.hops.common.item.ItemHops;
import growthcraft.hops.common.item.ItemHopSeeds;
import growthcraft.hops.common.village.ComponentVillageHopVineyard;
import growthcraft.hops.common.village.VillageHandlerHops;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.VillagerRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.init.Items;
import net.minecraft.potion.Potion;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.oredict.OreDictionary;

@Mod(
	modid = GrowthCraftHops.MOD_ID,
	name = GrowthCraftHops.MOD_NAME,
	version = GrowthCraftHops.MOD_VERSION,
	dependencies = "required-after:Growthcraft;required-after:Growthcraft|Cellar"
)
public class GrowthCraftHops
{
	public static final String MOD_ID = "Growthcraft|Hops";
	public static final String MOD_NAME = "Growthcraft Hops";
	public static final String MOD_VERSION = "@VERSION@";

	@Instance(MOD_ID)
	public static GrowthCraftHops instance;

	public static BlockTypeDefinition<BlockHops> hopVine;
	public static BlockBoozeDefinition[] hopAleFluids;

	public static ItemDefinition hops;
	public static ItemDefinition hopSeeds;
	public static ItemDefinition hopAle;
	public static ItemDefinition hopAleBucket_deprecated;
	public static ItemBucketBoozeDefinition[] hopAleBuckets;

	public static Fluid[] hopAleBooze;

	private GrcHopsConfig config = new GrcHopsConfig();
	private ModuleContainer modules = new ModuleContainer();

	public static GrcHopsConfig getConfig()
	{
		return instance.config;
	}

	@EventHandler
	public void preload(FMLPreInitializationEvent event)
	{
		config.load(event.getModConfigurationDirectory(), "growthcraft/hops.conf");

		if (config.enableThaumcraftIntegration) modules.add(new growthcraft.hops.integration.ThaumcraftModule());

		//====================
		// INIT
		//====================
		hopVine  = new BlockTypeDefinition<BlockHops>(new BlockHops());

		hops     = new ItemDefinition(new ItemHops());
		hopSeeds = new ItemDefinition(new ItemHopSeeds());

		hopAleBooze = new Booze[5];
		hopAleFluids = new BlockBoozeDefinition[hopAleBooze.length];
		hopAleBuckets = new ItemBucketBoozeDefinition[hopAleBooze.length];
		BoozeRegistryHelper.initializeBooze(hopAleBooze, hopAleFluids, hopAleBuckets, "grc.hopAle", config.hopAleColor);

		hopAle        = new ItemDefinition(new ItemBoozeBottle(5, -0.6F, hopAleBooze)
			.setColor(config.hopAleColor)
			.setTipsy(0.70F, 900)
			.setPotionEffects(new int[] {Potion.digSpeed.id}, new int[] {3600}));
		hopAleBucket_deprecated = new ItemDefinition(new ItemBoozeBucketDEPRECATED(hopAleBooze).setColor(config.hopAleColor));

		modules.preInit();
		register();
	}

	private void register()
	{
		//====================
		// REGISTRIES
		//====================
		GameRegistry.registerBlock(hopVine.getBlock(), "grc.hopVine");

		GameRegistry.registerItem(hops.getItem(), "grc.hops");
		GameRegistry.registerItem(hopSeeds.getItem(), "grc.hopSeeds");
		GameRegistry.registerItem(hopAle.getItem(), "grc.hopAle");
		GameRegistry.registerItem(hopAleBucket_deprecated.getItem(), "grc.hopAle_bucket");

		BoozeRegistryHelper.registerBooze(hopAleBooze, hopAleFluids, hopAleBuckets, hopAle, "grc.hopAle", hopAleBucket_deprecated);
		BoozeRegistryHelper.registerDefaultFermentation(hopAleBooze);

		CellarRegistry.instance().brewing().addBrewing(FluidRegistry.WATER, Items.wheat, hopAleBooze[4], config.hopAleBrewTime, config.hopAleBrewYield, Residue.newDefault(0.3F));
		CellarRegistry.instance().brewing().addBrewing(hopAleBooze[4], hops.getItem(), hopAleBooze[0], config.hopAleHoppedBrewTime, config.hopAleHoppedBrewYield, Residue.newDefault(0.0F));

		CoreRegistry.instance().addVineDrop(hops.asStack(2), config.hopsVineDropRarity);

		ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CORRIDOR).addItem(new WeightedRandomChestContent(hops.asStack(), 1, 2, 10));
		ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CROSSING).addItem(new WeightedRandomChestContent(hops.asStack(), 1, 2, 10));

		MapGenHelper.registerVillageStructure(ComponentVillageHopVineyard.class, "grc.hopvineyard");

		//====================
		// ORE DICTIONARY
		//====================
		OreDictionary.registerOre("cropHops", hops.getItem());
		OreDictionary.registerOre("materialHops", hops.getItem());
		OreDictionary.registerOre("seedHops", hopSeeds.getItem());
		// For Pam's HarvestCraft
		// Uses the same OreDict. names as HarvestCraft
		OreDictionary.registerOre("listAllseed", hopSeeds.getItem());

		//====================
		// CRAFTING
		//====================
		GameRegistry.addShapelessRecipe(hopSeeds.asStack(), hops.getItem());

		NEI.hideItem(hopVine.asStack());

		MinecraftForge.EVENT_BUS.register(this);

		modules.register();
	}

	@EventHandler
	public void load(FMLInitializationEvent event)
	{
		CommonProxy.instance.initRenders();

		final VillageHandlerHops handler = new VillageHandlerHops();
		VillagerRegistry.instance().registerVillageTradeHandler(GrowthCraftCellar.getConfig().villagerBrewerID, handler);
		VillagerRegistry.instance().registerVillageCreationHandler(handler);

		modules.init();
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onTextureStitchPost(TextureStitchEvent.Post event)
	{
		if (event.map.getTextureType() == 0)
		{
			for (int i = 0; i < hopAleBooze.length; ++i)
			{
				hopAleBooze[i].setIcons(GrowthCraftCore.liquidSmoothTexture);
			}
		}
	}

	@EventHandler
	public void postload(FMLPostInitializationEvent event)
	{
		modules.postInit();
	}
}
