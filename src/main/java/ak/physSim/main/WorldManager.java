package ak.physSim.main;

import ak.physSim.chunk.ChunkManager;
import ak.physSim.entity.Player;
import ak.physSim.render.Renderable;
import ak.physSim.voxel.Voxel;
import ak.physSim.voxel.VoxelType;
import com.flowpowered.noise.Noise;
import com.flowpowered.noise.NoiseQuality;
import org.joml.Vector3f;
import org.lwjgl.opengl.GLCapabilities;

import java.util.ArrayList;

/**
 * Created by Aleksander on 23/06/2016.
 */
public class WorldManager {
    private ChunkManager manager;
    private Player player;
    private GLCapabilities capabilities;

    public WorldManager(Player player, GLCapabilities capabilities){
        this.player = player;
        this.capabilities = capabilities;

        generateLandscape();

    }

    private void generateLines(){

        manager = new ChunkManager(capabilities);
        addVoxel(0, 0, 0, new Voxel(VoxelType.STONE));
        for (int i = 1; i < 10; i++) {
            drawRing(0, 0, 5 * i, 2);
        }
        manager.comupteAll();
    }

    private void drawRing(int x0, int z0, int radius, int height)
    {
        int x = radius;
        int z = 0;
        int err = 0;

        while (x >= z)
        {
            for (int y = 0; y < height; y++) {
                addVoxel(x0 + x, y, z0 + z, new Voxel(VoxelType.STONE));
                addVoxel(x0 + z, y, z0 + x, new Voxel(VoxelType.STONE));
                addVoxel(x0 - z, y, z0 + x, new Voxel(VoxelType.STONE));
                addVoxel(x0 - x, y, z0 + z, new Voxel(VoxelType.STONE));
                addVoxel(x0 - x, y, z0 - z, new Voxel(VoxelType.STONE));
                addVoxel(x0 - z, y, z0 - x, new Voxel(VoxelType.STONE));
                addVoxel(x0 + z, y, z0 - x, new Voxel(VoxelType.STONE));
                addVoxel(x0 + x, y, z0 - z, new Voxel(VoxelType.STONE));
            }

            z += 1;
            err += 1 + 2*z;
            if (2*(err-x) + 1 > 0)
            {
                x -= 1;
                err += 1 - 2*x;
            }
        }
    }
    private void generateLandscape() {
        manager = new ChunkManager(capabilities);
        for (int x = -100; x < 100; x++) {
            for (int z = -100; z < 100; z++) {
                int height = 5 + (int) ((Noise.gradientCoherentNoise3D(x/32f, 0, z/32f, 23423, NoiseQuality.FAST) + 1)/2 * 60);
                for (int y = 0; y < height; y++) {
                    if (y < 10)
                        addVoxel(x, y, z, new Voxel(VoxelType.DARK_STONE));
                    else
                        addVoxel(x, y, z, new Voxel(VoxelType.STONE));
                }
                addVoxel(x, height, z, new Voxel(VoxelType.STONE));

            }
        }

        manager.comupteAll();
    }

    public void addVoxel(int x, int y, int z, Voxel voxel){
        manager.addVoxel(x, y, z, voxel);
    }

    public ArrayList<Renderable> getObjectsToRender() {
        return manager.getChunks();
    }

    public void cleanup() {
        manager.cleanup();
    }

    public void addVoxel(Vector3f position, Voxel voxel) {
        addVoxel((int) position.x, (int) position.y, (int) position.z, voxel);
    }
}
