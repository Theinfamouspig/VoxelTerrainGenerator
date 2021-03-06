package ak.physSim.map;

import ak.physSim.entity.Collision;
import ak.physSim.entity.Player;
import ak.physSim.map.chunk.Chunk;
import ak.physSim.map.generator.TerrainGenerator;
import ak.physSim.util.Point3d;
import ak.physSim.voxel.Voxel;
import ak.physSim.voxel.VoxelType;
import com.flowpowered.noise.Noise;
import com.flowpowered.noise.NoiseQuality;
import org.joml.Vector3f;
import org.lwjgl.opengl.GLCapabilities;

import java.util.ArrayList;

import static ak.physSim.util.Reference.CHUNK_SIZE;

/**
 * Created by Aleksander on 23/06/2016.
 */
public class WorldManager {

    private ChunkManager manager;
    private Player player;
    private TerrainGenerator generator;
    private Collision collision = new Collision();
    private boolean[] voxelTest = new boolean[]{
            false, false, false,
            true,  false, false,
            true,  false, true,
            false, false, true,
            false, true,  false,
            true,  true,  false,
            true,  true,  true,
            false, true,  true,
    };
    /*
        Terrain generation
     */

    public WorldManager(Player player, ChunkManager manager) {
        this.player = player;
        this.manager = manager;

        //generateLandscape(80, 80);
        //generateBlobs();

        //generatePlane(10, 0, 0, 0, CHUNK_SIZE, CHUNK_SIZE, 1, VoxelType.STONE);

        //for (int i = 0; i <= 4; i++)
            //generatePlane(5 - i, 0, i, 0, CHUNK_SIZE, CHUNK_SIZE, 1, VoxelType.values()[i]);

        //generatePlane(10, 0, 0, 0);
        //generateLandscape(100, 100);

        int size = 1000;
        generator = new TerrainGenerator(45634456l, size, size, 5);
        generateLandscape(size, size);
    }

    private void generatePlane(int size, int offsetX, int offsetY, int offsetZ, VoxelType type) {
        generatePlane(size, offsetX, offsetY, offsetZ, CHUNK_SIZE, CHUNK_SIZE, CHUNK_SIZE, type);
    }

    private void generatePlane(int size, int offsetX, int offsetY, int offsetZ, int xSize, int ySize, int zSize, VoxelType type) {
        for (int x = -size; x < size; x++) {
            for (int z = -size; z < size; z++) {
                for (int cX = 0; cX < xSize; cX++) {
                    for (int cZ = 0; cZ < ySize; cZ++) {
                        for (int cY = 0; cY < zSize; cY++) {
                            manager.addVoxel(cX + x * 16, cY, cZ + z*16, type);
                        }
                    }
                }
            }
        }
    }

    private void generateLines(int y){
        for (int i = 1; i < 10; i++) {
                drawRing(0, y, 0,   5 * i, 3*i, VoxelType.STONE);
                drawRing(0, y + 3*i, 0, 5 * i, 1, VoxelType.LIGHT);
        }
    }

    private void drawRing(int x0, int y0, int z0, int radius, int height, VoxelType type){
        int x = radius;
        int z = 0;
        int err = 0;

        while (x >= z)
        {
            for (int y = y0; y < height+y0; y++) {
                addVoxel(x0 + x, y, z0 + z, type);
                addVoxel(x0 + z, y, z0 + x, type);
                addVoxel(x0 - z, y, z0 + x, type);
                addVoxel(x0 - x, y, z0 + z, type);
                addVoxel(x0 - x, y, z0 - z, type);
                addVoxel(x0 - z, y, z0 - x, type);
                addVoxel(x0 + z, y, z0 - x, type);
                addVoxel(x0 + x, y, z0 - z, type);
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
                int height = (int) (generator.getElevation(x, z) * 60);
                addVoxel(x, 0, z, VoxelType.WATER);
                for (int y = 1; y < height - 1; y++) {
                    addVoxel(x, y, z, VoxelType.STONE);
                }
            }
        }
        player.setPosition(0, (int) (generator.getElevation(0, 0) * 60 + 1), 0);
    }

    private void generateBlobs(){
        int rad = 10;
        for (int x = -rad; x < rad; x++) {
            for (int y = 0; y < 5; y++) {
                for (int z = -rad; z < rad; z++) {
                    generateBlobChunk(x, y, z);
                }
            }
        }
    }

    private void generateBlobChunk(int x, int y, int z) {
        for (int vX = 0; vX < CHUNK_SIZE; vX++) {
            for (int vY = 0; vY < CHUNK_SIZE; vY++) {
                for (int vZ = 0; vZ < CHUNK_SIZE; vZ++) {
                    if ((Noise.gradientCoherentNoise3D((CHUNK_SIZE * x + vX)/ (float) CHUNK_SIZE, (CHUNK_SIZE * y + vY)/ (float) CHUNK_SIZE, (CHUNK_SIZE * z + vZ)/ (float) CHUNK_SIZE, 43, NoiseQuality.BEST) + 1)/2 > 0.8)
                        manager.addVoxel(vX+ x*16, vY+ y*16, vZ+ z*16, VoxelType.STONE);
                }
            }
        }
    }

    public void generateChunk(float x1, float y1, float z1) {
        int x = (int) x1;
        int y = (int) y1;
        int z = (int) z1;
        for (int vX = 0; vX < 16; vX++) {
            for (int vY = 0; vY < 16; vY++) {
                for (int vZ = 0; vZ < 16; vZ++) {
                    if ((Noise.gradientCoherentNoise3D((16 * x + vX)/16f, (16 * y + vY)/16f, (16 * z + vZ)/16f, 43, NoiseQuality.BEST) + 1)/2 > 0.8)
                        manager.addVoxel(vX+ x * 16, vY+ y * 16, vZ + z * 16, VoxelType.STONE);
                }
            }
        }
    }

    /*
        Methods for terrain
     */

    public void addVoxel(int x, int y, int z, VoxelType voxel){
        manager.addVoxel(x, y, z, voxel);
    }

    public void addVoxel(Vector3f position, VoxelType voxel) {
        addVoxel((int) position.x, (int) position.y, (int) position.z, voxel);
    }

    public Collision checkVoxelCollision(Vector3f position, Vector3f lower, Vector3f higher) {
        int lowerX =  (int) Math.floor(position.x + lower.x);
        int lowerY =  (int) Math.floor(position.y + lower.y);
        int lowerZ =  (int) Math.floor(position.z + lower.z);
        int higherX = (int) Math.floor(position.x + higher.x);
        int higherY = (int) Math.floor(position.y + higher.y);
        int higherZ = (int) Math.floor(position.z + higher.z);
        //LowerX,   HigherY, HigherZ;
        //HigherX,  HigherY, HigherZ;
        //LowerX,   LowerY,  HigherZ;
        //HigherX,  LowerY,  HigherZ;
        //LowerX,   HigherY, LowerZ;
        //HigherX,  HigherY, LowerZ;
        //LowerX,   LowerY,  LowerZ;
        //HigherX,  LowerY,  LowerZ;
        Voxel testing;

        for (int i = 0; i < voxelTest.length;) {
            //loop through the 8 vertices of a box
            int posX = voxelTest[i++] ? lowerX : higherX;
            int posY = voxelTest[i++] ? lowerY : higherY;
            int posZ = voxelTest[i++] ? lowerZ : higherZ;
            testing = manager.getVoxel(posX, posY, posZ);
            if (testing != null)
//                return new Vector3f(posX, posY, posZ);
                return null;
        }
        return null;
    }

    public void update(float delta) {
        player.update(delta, this);
    }

    /*
        Cleanup and others
     */
    public ArrayList<Chunk> getObjectsToRender(float distance) {
        return manager.getChunks(distance);
    }

    public void cleanup() {
        manager.cleanup();
    }
}
