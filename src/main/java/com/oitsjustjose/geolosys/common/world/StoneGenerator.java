package com.oitsjustjose.geolosys.common.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.oitsjustjose.geolosys.Geolosys;
import com.oitsjustjose.geolosys.common.api.GeolosysAPI;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fml.common.IWorldGenerator;

/**
 * A modified version of:
 * https://github.com/BluSunrize/ImmersiveEngineering/blob/master/src/main/java/blusunrize/immersiveengineering/common/world/IEWorldGen.java
 * Original Source & Credit: BluSunrize
 **/

public class StoneGenerator implements IWorldGenerator
{
    private static final List<IBlockState> blockStateMatchers = GeolosysAPI.replacementMats;
    private static final String dataID = "geolosysStoneGeneratorPending";
    private static ArrayList<StoneGen> stoneSpawnList = new ArrayList<>();
    private static int overallWeight = 0;

    public static void addStoneGen(IBlockState state, int minY, int maxY, int weight)
    {
        StoneGen gen = new StoneGen(state, minY, maxY, weight);
        stoneSpawnList.add(gen);
        overallWeight += weight;
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
            IChunkProvider chunkProvider)
    {
        ToDoBlocks.getForWorld(world, dataID).processPending(new ChunkPos(chunkX, chunkZ), world, blockStateMatchers);
        if (world.provider.getDimension() == 1 || world.provider.getDimension() == -1)
        {
            return;
        }
        if (overallWeight > 0)
        {
            boolean generated = false;
            while (!generated)
            {
                int rng = random.nextInt(overallWeight);
                StoneGen sg = stoneSpawnList.get(random.nextInt(stoneSpawnList.size()));
                if (sg.weight >= rng)
                {
                    sg.generate(world, random, (chunkX * 16), (chunkZ * 16));
                    generated = true;
                }
            }
        }
    }

    public static class StoneGen
    {
        WorldGenMinableSafe pluton;
        IBlockState state;
        int minY;
        int maxY;
        int weight;

        StoneGen(IBlockState state, int minY, int maxY, int weight)
        {
            this.pluton = new WorldGenMinableSafe(state, 96, blockStateMatchers, dataID);
            this.state = state;
            this.minY = Math.min(minY, maxY);
            this.maxY = Math.max(minY, maxY);
            this.weight = weight;
        }

        public void generate(World world, Random rand, int x, int z)
        {
            if (!Geolosys.getInstance().chunkOreGen.canGenerateInChunk(world, new ChunkPos(x / 16, z / 16),
                    world.provider.getDimension()))
            {
                return;
            }
            boolean lastState = ForgeModContainer.logCascadingWorldGeneration;
            ForgeModContainer.logCascadingWorldGeneration = false;
            if (rand.nextInt(100) < weight)
            {
                int y = minY != maxY ? minY + rand.nextInt(maxY - minY) : minY;
                pluton.generate(world, rand, new BlockPos(x, y, z));
            }
            ForgeModContainer.logCascadingWorldGeneration = lastState;
        }
    }
}
