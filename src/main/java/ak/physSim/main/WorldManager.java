package ak.physSim.main;

import ak.physSim.chunk.Chunk;
import ak.physSim.chunk.ChunkManager;
import ak.physSim.entity.Player;
import ak.physSim.render.Renderable;
import ak.physSim.util.Point3d;
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

    public WorldManager(Player player, GLCapabilities capabilities) {
        this.player = player;
        this.capabilities = capabilities;

        //generateLandscape(80, 80);
        //generateBlobs();

        manager = new ChunkManager(capabilities);
        generatePlane(10, 0, 0, 0);
        generatePlane(5, 0, 10, 0);
        generateLines();
        generatePillars();
        //generateBlobs();
        //generateLandscape(100, 100);
    }

    private void generatePillars() {

    }

    private void generatePlane(int size, int offsetX, int offsetY, int offsetZ) {
        for (int x = -size; x < size; x++) {
            for (int z = -size; z < size; z++) {
                Chunk c = new Chunk(x + offsetX, offsetY, z + offsetZ);
                for (int cX = 0; cX < 16; cX++) {
                    for (int cZ = 0; cZ < 16; cZ++) {
                        for (int cY = 0; cY < 16; cY++) {
                            c.setVoxel(cX, cY, cZ, new Voxel(VoxelType.STONE));
                        }
                    }
                }
                manager.addChunk(x + offsetX, offsetY, z + offsetZ, c);
                c.setup(capabilities);
            }
        }
    }

    private void generateLines(){


        for (int i = 1; i < 10; i++) {
                drawRing(0, 0, 0,   5 * i, 3*i, VoxelType.STONE);
                drawRing(0, 3*i, 0, 5 * i, 1, VoxelType.GRASS);
        }
    }

    private void drawRing(int x0, int y0, int z0, int radius, int height, VoxelType type){
        int x = radius;
        int z = 0;
        int err = 0;

        while (x >= z)
        {
            for (int y = y0; y < height+y0; y++) {
                addVoxel(x0 + x, y, z0 + z, new Voxel(type));
                addVoxel(x0 + z, y, z0 + x, new Voxel(type));
                addVoxel(x0 - z, y, z0 + x, new Voxel(type));
                addVoxel(x0 - x, y, z0 + z, new Voxel(type));
                addVoxel(x0 - x, y, z0 - z, new Voxel(type));
                addVoxel(x0 - z, y, z0 - x, new Voxel(type));
                addVoxel(x0 + z, y, z0 - x, new Voxel(type));
                addVoxel(x0 + x, y, z0 - z, new Voxel(type));
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

    private void generateLandscape(int xS, int zS) {
        for (int x = -xS; x < xS; x++) {
            for (int z = -zS; z < zS; z++) {
                int height = 5 + (int) ((Noise.gradientCoherentNoise3D(x/60f, 0, z/60f, 23, NoiseQuality.BEST) + 1)/2 * 60);
                for (int y = 0; y < height; y++) {
                    if (y < 10)
                        addVoxel(x, y, z, new Voxel(VoxelType.DARK_STONE));
                    else
                        addVoxel(x, y, z, new Voxel(VoxelType.STONE));
                }
                addVoxel(x, height, z, new Voxel(VoxelType.GRASS));

            }
        }
        player.setPosition(0, 7 + (int) ((Noise.gradientCoherentNoise3D(0, 0, 0, 23, NoiseQuality.BEST) + 1) / 2 * 160), 0);
    }

    private void generateBlobs(){
        int rad = 10;
        for (int x = -rad; x < rad; x++) {
            for (int y = 0; y < 5; y++) {
                for (int z = -rad; z < rad; z++) {
                    manager.addChunk(new Point3d(x, y, z), generateBlobChunk(x, y, z));
                }
            }
        }
    }

    private Chunk generateBlobChunk(int x, int y, int z) {
        Chunk c = new Chunk(x, y, z);
        c.setup(capabilities);
        for (int vX = 0; vX < 16; vX++) {
            for (int vY = 0; vY < 16; vY++) {
                for (int vZ = 0; vZ < 16; vZ++) {
                    if ((Noise.gradientCoherentNoise3D((16 * x + vX)/16f, (16 * y + vY)/16f, (16 * z + vZ)/16f, 43, NoiseQuality.BEST) + 1)/2 > 0.8)
                        c.setVoxel(vX, vY, vZ, new Voxel(VoxelType.STONE));
                }
            }
        }
        return c;
    }

    public void addVoxel(int x, int y, int z, Voxel voxel){
        manager.addVoxel(x, y, z, voxel);
    }

    public ArrayList<Chunk> getObjectsToRender() {
        return manager.getChunks();
    }

    public void cleanup() {
        manager.cleanup();
    }

    public void addVoxel(Vector3f position, Voxel voxel) {
        addVoxel((int) position.x, (int) position.y, (int) position.z, voxel);
    }

    public void generateChunk(float x1, float y1, float z1) {
        int x = (int) x1;
        int y = (int) y1;
        int z = (int) z1;
        Chunk c = new Chunk(x, y, z);
        c.setup(capabilities);
        for (int vX = 0; vX < 16; vX++) {
            for (int vY = 0; vY < 16; vY++) {
                for (int vZ = 0; vZ < 16; vZ++) {
                    if ((Noise.gradientCoherentNoise3D((16 * x + vX)/16f, (16 * y + vY)/16f, (16 * z + vZ)/16f, 43, NoiseQuality.BEST) + 1)/2 > 0.8)
                        c.setVoxel(vX, vY, vZ, new Voxel(VoxelType.STONE));
                }
            }
        }
        manager.addChunk(x, y, z, c);
    }

    public void updatePosition() {

    }
}
