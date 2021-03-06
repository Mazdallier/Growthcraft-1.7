package growthcraft.api.cellar.heatsource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.annotation.Nonnull;

import net.minecraft.block.Block;

public class HeatSourceRegistry implements IHeatSourceRegistry
{
	static class HeatMap extends HashMap<Integer, IHeatSourceBlock>
	{
		public static final long serialVersionUID = 1L;
	}

	static class HeatSourceTree extends HashMap<Block, HeatMap>
	{
		public static final long serialVersionUID = 1L;
	}

	public static final float DEFAULT_HEAT = 1.0f;
	public static final float NO_HEAT = 0.0f;
	private static final int NO_META = -1;

	private HeatSourceTree heatSources = new HeatSourceTree();

	public void addHeatSource(@Nonnull Block block, int meta, IHeatSourceBlock heat)
	{
		if (!heatSources.containsKey(block))
		{
			heatSources.put(block, new HeatMap());
		}
		final HeatMap map = heatSources.get(block);
		map.put(meta, heat);
	}

	public void addHeatSource(@Nonnull Block block, int meta, float heat)
	{
		addHeatSource(block, meta, new GenericHeatSourceBlock(block, heat));
	}

	public void addHeatSource(@Nonnull Block block, int meta)
	{
		addHeatSource(block, meta, DEFAULT_HEAT);
	}

	public void addHeatSource(@Nonnull Block block, IHeatSourceBlock heat)
	{
		addHeatSource(block, NO_META, heat);
	}

	public void addHeatSource(@Nonnull Block block)
	{
		addHeatSource(block, NO_META);
	}

	public IHeatSourceBlock getHeatSource(Block block, int meta)
	{
		final HeatMap map = heatSources.get(block);
		if (map == null) return null;

		IHeatSourceBlock f = map.get(meta);
		if (f == null) f = map.get(NO_META);
		if (f == null) return null;
		return f;
	}

	public IHeatSourceBlock getHeatSource(Block block)
	{
		return getHeatSource(block, NO_META);
	}

	public boolean isBlockHeatSource(Block block, int meta)
	{
		final HeatMap map = heatSources.get(block);
		if (map == null) return false;
		return map.get(meta) != null || map.get(NO_META) != null;
	}

	public boolean isBlockHeatSource(Block block)
	{
		return isBlockHeatSource(block, NO_META);
	}
}
