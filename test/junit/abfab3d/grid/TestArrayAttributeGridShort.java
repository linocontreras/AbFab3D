/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.grid;

// External Imports

import abfab3d.core.AttributeGrid;
import abfab3d.core.Grid;
import abfab3d.core.Bounds;
import junit.framework.Test;
import junit.framework.TestSuite;

// Internal Imports

/**
 * Tests the functionality of a ArrayAttributeGridShort.
 *
 * @author Alan Hudson
 */
public class TestArrayAttributeGridShort extends BaseTestAttributeGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestArrayAttributeGridShort.class);
    }

    public void testToString() {
        Grid grid = new ArrayAttributeGridShort(1, 1, 1, 0.001, 0.001);

        runToString(grid);
    }

    /**
     * Test the constructors and the grid size.
     */
    public void testConstructors() {
        AttributeGrid grid = new ArrayAttributeGridShort(1, 1, 1, 0.001, 0.001);
        assertEquals("Array size is not 1", 1, grid.getWidth() * grid.getHeight() * grid.getDepth());

        grid = new ArrayAttributeGridShort(100, 101, 102, 0.001, 0.001);
        assertEquals("Array size is not 1030200", 1030200, grid.getWidth() * grid.getHeight() * grid.getDepth());

        grid = new ArrayAttributeGridShort(new Bounds(1.0, 1.0, 1.0), 0.2, 0.1);
        assertEquals("Array size is not 250", 250, grid.getWidth() * grid.getHeight() * grid.getDepth());

        // grid size should be 6x6x11
        grid = new ArrayAttributeGridShort(new Bounds(1.1, 1.1, 1.1), 0.2, 0.1);
        assertEquals("Array size is not 396", 396, grid.getWidth() * grid.getHeight() * grid.getDepth());

        try {
            // test > int index size
            grid = new ArrayAttributeGridShort(10000,10000,10000, 0.2,0.1);
            fail("Index size check failed");
        } catch(IllegalArgumentException iae) {
            // passed
        }

    }

    /**
     * Test creating an empty grid.
     */
    public void testCreateEmpty() {
        AttributeGrid grid = new ArrayAttributeGridShort(100, 101, 102, 0.001, 0.001);

        createEmpty(grid);
    }

    /**
     * Test clone.
     */
    public void testClone() {
        int size = 10;
        double voxelSize = 0.002;
        double sliceHeight = 0.001;

        Grid grid = new ArrayAttributeGridShort(size,size,size,voxelSize,sliceHeight);
        runClone(grid);
    }

    /**
     * Test set/get all data points.
     */
    public void testSetGetByVoxelCoords() {
        AttributeGrid grid = new ArrayAttributeGridShort(1, 1, 1, 0.001, 0.001);
        setGetAllVoxelCoords(grid);

        grid = new ArrayAttributeGridShort(3, 2, 2, 0.001, 0.001);
        setGetAllVoxelCoords(grid);

        grid = new ArrayAttributeGridShort(11, 11, 11, 0.001, 0.001);
        setGetAllVoxelCoords(grid);

        grid = new ArrayAttributeGridShort(100, 91, 85, 0.001, 0.001);
        setGetAllVoxelCoords(grid);
    }

    /**
     * Test set/get all data points.
     */
    public void testSetGetByWorldCoords() {
        AttributeGrid grid = new ArrayAttributeGridShort(1, 1, 1, 0.001, 0.001);
        setGetAllVoxelByWorldCoords(grid);

        grid = new ArrayAttributeGridShort(3, 2, 2, 0.001, 0.001);
        setGetAllVoxelByWorldCoords(grid);

        grid = new ArrayAttributeGridShort(11, 11, 11, 0.001, 0.001);
        setGetAllVoxelByWorldCoords(grid);

        grid = new ArrayAttributeGridShort(100, 91, 85, 0.001, 0.001);
        setGetAllVoxelByWorldCoords(grid);
    }

    /**
     * Test getState by voxels.
     */
    public void testGetStateByVoxel() {
        AttributeGrid grid = new ArrayAttributeGridShort(10, 9, 8, 0.001, 0.001);
        getStateByVoxel(grid);
    }

    /**
     * Test getData by voxels.
     */
    public void testGetDataByVoxel() {
        AttributeGrid grid = new ArrayAttributeGridShort(10, 9, 8, 0.001, 0.001);
        getDataByVoxel(grid);
    }

    /**
     * Test getData by voxels.
     */
    public void testGetDataByCoord() {
        AttributeGrid grid = new ArrayAttributeGridShort(new Bounds(1.0, 0.4, 0.5), 0.05, 0.01);
        getDataByCoord(grid);
    }

    /**
     * Test getState by world coordinates.
     */
    public void testGetStateByCoord() {
        AttributeGrid grid = new ArrayAttributeGridShort(new Bounds(1.0, 0.4, 0.5), 0.05, 0.01);
        getStateByCoord1(grid);

        // should expect width=3, height=6, depth=4
        // set data for a mid-voxel and test the bounds
        grid = new ArrayAttributeGridShort(new Bounds(0.15, 0.12, 0.20), 0.05, 0.02);
        getStateByCoord2(grid);
    }

    /**
     * Test getAttribute by voxels.
     */
    public void testGetMaterialByVoxel() {
        AttributeGrid grid = new ArrayAttributeGridShort(10, 9, 8, 0.001, 0.001);
        getMaterialByVoxel(grid);
    }

    /**
     * Test getAttribute by world coordinates.
     */
    public void testGetMaterialByCoord() {
        AttributeGrid grid = new ArrayAttributeGridShort(new Bounds(1.0, 0.4, 0.5), 0.05, 0.01);
        getMaterialByCoord1(grid);


        // should expect width=3, height=6, depth=4
        // set data for a mid-voxel and test the bounds
        grid = new ArrayAttributeGridShort(new Bounds(0.15, 0.12, 0.20), 0.05, 0.02);
        getMaterialByCoord2(grid);
    }

    /**
     * Test setAttribute.
     */
    public void testsetAttribute() {
        int size = 10;

        AttributeGrid grid = new ArrayAttributeGridShort(size, size, size, 0.001, 0.001);
        setAttribute(grid);
    }

    public void testSetAttributeShort() {
        AttributeGrid grid = new ArrayAttributeGridShort(78, 112, 26, 0.001, 0.001);

        grid.setAttribute(0, 0, 0, 1);
        grid.setAttribute(9, 9, 9, 256);
        grid.setAttribute(63, 111, 24, 1000);

        System.out.println("Val: " + grid.getAttribute(63,111,24));
        // check that the material changed, but the state did not
        assertEquals("Material should be ", 1, grid.getAttribute(0, 0, 0));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(0, 0, 0));

        assertEquals("Material should be ", 256, grid.getAttribute(9, 9, 9));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(9, 9, 9));

        assertEquals("Material should be ", 1000, grid.getAttribute(63, 111, 24));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(63, 111, 24));
    }

    /**
     * Test setState.
     */
    public void testSetState() {
        int size = 10;

        AttributeGrid grid = new ArrayAttributeGridShort(size, size, size, 0.001, 0.001);
        setState(grid);
    }

    /**
     * Test reassignAttribute.
     */
    public void testReassignMaterial() {
        int size = 20;

        AttributeGrid grid = new ArrayAttributeGridShort(size, size, size, 0.001, 0.001);
        reassignMaterial(grid);
    }

    /**
     * Test set/get short material range.
     */
    public void testShortMaterialRange() {
        int width = 100;

        AttributeGrid grid = new ArrayAttributeGridShort(width, 1, 1, 0.001, 0.001);
        shortMaterialRange(grid);
    }

    /**
     * Test findCount by voxel class.
     */
    public void testFindCountByVoxelClass() {
        int width = 6;
        int height = 3;
        int depth = 10;

        AttributeGrid grid = new ArrayAttributeGridShort(width, height, depth, 0.05, 0.02);
        findCountByVoxelClass(grid);

    }

    /**
     * Test findCount by material.
     */
    public void testFindCountByMat() {
        int width = 3;
        int height = 4;
        int depth = 10;

        AttributeGrid grid = new ArrayAttributeGridShort(width, height, depth, 0.05, 0.02);
        findCountByMat(grid);
    }

    /**
     * Test find voxels by voxel class
     */
    public void testFindVoxelClass() {
        int width = 3;
        int height = 4;
        int depth = 10;

        AttributeGrid grid = new ArrayAttributeGridShort(width, height, depth, 0.05, 0.02);
        findVoxelClass(grid);
    }

    /**
     * Test that find voxels by VoxelClass actually found the voxels in the correct coordinates
     */
    public void testFindVoxelClassIterator() {
        int width = 20;
        int height = 10;
        int depth = 10;

        AttributeGrid grid = new ArrayAttributeGridShort(width, height, depth, 0.001, 0.001);
        findVoxelClassIterator1(grid);

        grid = new ArrayAttributeGridShort(width, height, depth, 0.001, 0.001);
        findVoxelClassIterator2(grid);
    }

    /**
     * Test that find voxels by VoxelClass actually found the voxels in the correct coordinates
     */
    public void testFindInterruptableVoxelClassIterator() {
        int width = 20;
        int height = 10;
        int depth = 10;

        AttributeGrid grid = new ArrayAttributeGridShort(width, height, depth, 0.001, 0.001);
        findInterruptableVoxelClassIterator1(grid);
        grid = new ArrayAttributeGridShort(width, height, depth, 0.001, 0.001);
        findInterruptableVoxelClassIterator2(grid);
    }

    /**
     * Test that find voxels by material actually found the voxels in the correct coordinates
     */
    public void testFindMaterialIterator() {
        int width = 20;
        int height = 10;
        int depth = 10;

        AttributeGrid grid = new ArrayAttributeGridShort(width, height, depth, 0.001, 0.001);
        findMaterialIterator1(grid);
        grid = new ArrayAttributeGridShort(width, height, depth, 0.001, 0.001);
        findMaterialIterator2(grid);
    }

    /**
     * Test that find voxels by material actually found the voxels in the correct coordinates
     */
    public void testFindInterruptablMaterialIterator() {
        int width = 20;
        int height = 10;
        int depth = 10;

        AttributeGrid grid = new ArrayAttributeGridShort(width, height, depth, 0.001, 0.001);
        findInterruptablMaterialIterator1(grid);

        grid = new ArrayAttributeGridShort(width, height, depth, 0.001, 0.001);
        findInterruptablMaterialIterator2(grid);
    }

    /**
     * Test that find voxels by VoxelClass and material actually found the voxels in the correct coordinates
     */
    public void testFindMaterialAndVCIterator() {
        int width = 20;
        int height = 10;
        int depth = 10;

        AttributeGrid grid = new ArrayAttributeGridShort(width, height, depth, 0.001, 0.001);
        findMaterialAndVCIterator1(grid);
        grid = new ArrayAttributeGridShort(width, height, depth, 0.001, 0.001);
        findMaterialAndVCIterator2(grid);
    }

    /**
     * Test that find voxels by voxel class and material actually found the voxels in the correct coordinates
     */
    public void testFindInterruptablMaterialAndVCIterator() {
        int width = 20;
        int height = 10;
        int depth = 10;

        AttributeGrid grid = new ArrayAttributeGridShort(width, height, depth, 0.001, 0.001);
        findInterruptablMaterialAndVCIterator1(grid);
        grid = new ArrayAttributeGridShort(width, height, depth, 0.001, 0.001);
        findInterruptablMaterialAndVCIterator2(grid);
    }

    /**
     * Test getGridCoords.
     */
    public void testGetGridCoords() {
        double xWorldCoord = 1.0;
        double yWorldCoord = 0.15;
        double zWorldCoord = 0.61;
        double voxelWidth = 0.02;
        double sliceHeight = 0.01;

        AttributeGrid grid = new ArrayAttributeGridShort(new Bounds(xWorldCoord, yWorldCoord, zWorldCoord), voxelWidth, sliceHeight);
        getGridCoords(grid);
    }

    /**
     * Test getWorldCoords.
     */
    public void testGetWorldCoords() {
        int xVoxels = 50;
        int yVoxels = 15;
        int zVoxels = 31;
        double voxelWidth = 0.02;
        double sliceHeight = 0.01;

        AttributeGrid grid = new ArrayAttributeGridShort(xVoxels, yVoxels, zVoxels, voxelWidth, sliceHeight);
        getWorldCoords(grid);
    }

    /**
     * Test getWorldCoords.
     */
    public void testGetGridBounds() {
        int xVoxels = 50;
        int yVoxels = 15;
        int zVoxels = 31;
        double voxelWidth = 0.02;
        double sliceHeight = 0.01;

        AttributeGrid grid = new ArrayAttributeGridShort(xVoxels, yVoxels, zVoxels, voxelWidth, sliceHeight);
        getGridBounds(grid);
    }

    /**
     * Test getWidth with both constructor methods.
     */
    public void testGetWidth() {
        int width = 70;

        // voxel coordinates
        AttributeGrid grid = new ArrayAttributeGridShort(width, 50, 25, 0.05, 0.01);
        assertEquals("Width is not " + width, width, grid.getWidth());

        // world coordinates
        double xcoord = 0.12;
        double voxelSize = 0.05;
        width = BaseGrid.roundSize(xcoord / voxelSize);

        grid = new ArrayAttributeGridShort(new Bounds(xcoord, 0.11, 0.16), voxelSize, 0.02);
        assertEquals("Width is not " + width, width, grid.getWidth());
    }

    /**
     * Test getHeight with both constructor methods.
     */
    public void testGetHeight() {
        int height = 70;

        // voxel coordinates
        AttributeGrid grid = new ArrayAttributeGridShort(50, height, 25, 0.05, 0.02);
        assertEquals("Height is not " + height, height, grid.getHeight());

        // world coordinates
        double ycoord = 0.11;
        double sliceHeight = 0.02;
        height = BaseGrid.roundSize(ycoord / sliceHeight);

        grid = new ArrayAttributeGridShort(new Bounds(0.12, ycoord, 0.16), 0.05, sliceHeight);
        assertEquals("Height is not " + height, height, grid.getHeight());
    }

    /**
     * Test getDepth with both constructor methods.
     */
    public void testGetDepth() {
        int depth = 70;

        // voxel coordinates
        AttributeGrid grid = new ArrayAttributeGridShort(50, 25, depth, 0.05, 0.01);
        assertEquals("Depth is not " + depth, depth, grid.getDepth());

        // world coordinates
        double zcoord = 0.12;
        double voxelSize = 0.05;
        depth = BaseGrid.roundSize(zcoord / voxelSize);

        grid = new ArrayAttributeGridShort(new Bounds(0.12, 0.11, zcoord), voxelSize, 0.02);
        assertEquals("Depth is not " + depth, depth, grid.getDepth());
    }

    /**
     * Test getSliceHeight with both constructor methods.
     */
    public void testGetSliceHeight() {
        double sliceHeight = 0.0015;

        // voxel coordinates
        AttributeGrid grid = new ArrayAttributeGridShort(50, 25, 70, 0.05, sliceHeight);
        assertEquals("Slice height is not " + sliceHeight, sliceHeight, grid.getSliceHeight());

        // world coordinates
        grid = new ArrayAttributeGridShort(new Bounds(0.12, 0.11, 0.12), 0.05, sliceHeight);
        assertEquals("Slice height is not" + sliceHeight, sliceHeight, grid.getSliceHeight());
    }

    /**
     * Test getVoxelSize with both constructor methods.
     */
    public void testGetVoxelSize() {
        double voxelSize = 0.025;

        // voxel coordinates
        AttributeGrid grid = new ArrayAttributeGridShort(50, 25, 70, voxelSize, 0.01);
        assertEquals("Voxel size is not " + voxelSize, voxelSize, grid.getVoxelSize());

        // world coordinates
        grid = new ArrayAttributeGridShort(new Bounds(0.12, 0.11, 0.12), voxelSize, 0.01);
        assertEquals("Voxel size is not " + voxelSize, voxelSize, grid.getVoxelSize());
    }

    /**
     * Test that remove material removes all specified material
     */
    public void testRemoveMaterialIterator() {
        int width = 20;
        int height = 10;
        int depth = 10;

        AttributeGrid grid = new ArrayAttributeGridShort(width, height, depth, 0.001, 0.001);
        removeMaterialIterator(grid);
    }
}
