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

package abfab3d.datasources;

// External Imports


// external imports
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.vecmath.Vector3d;

// Internal Imports

import abfab3d.core.Vec;
import abfab3d.core.Bounds;
import abfab3d.core.DataSource;
import abfab3d.core.AttributeGrid;

import abfab3d.util.ColorMapperDistance;
import abfab3d.util.ColorMapper;

import abfab3d.grid.op.GridMaker;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Units.MM;
import static abfab3d.grid.util.GridUtil.writeSlice;


/**
 * Tests the functionality of Sphere
 *
 * @version
 */
public class TestSphere extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestSphere.class);
    }

    public void testSphereDistance() {

        printf("testSphereDistance()\n");
        Sphere sphere = new Sphere(new Vector3d(1,0,0), 1.);
        sphere.setDataType(DataSource.DATA_TYPE_DISTANCE);
        sphere.initialize();
        Vec pnt = new Vec(3);
        Vec data = new Vec(3);
        double coord[][] = new double[][]{{0,0,0, 0},{0.5,0,0, -0.5},{1.5,0,0, -0.5},{2.5,0,0, 0.5},};
        
        for(int i = 0; i < coord.length; i++){
            double cc[] = coord[i];
            pnt.v[0] = cc[0];
            pnt.v[1] = cc[1];
            pnt.v[2] = cc[2];
            sphere.getDataValue(pnt, data);
            printf("pnt: [%7.5f  %7.5f  %7.5f] data: %7.5f expect: %7.5f\n", cc[0], cc[1], cc[2], data.v[0], cc[3]);            
            assertTrue(fmt("data.v[0]==cc[3]: %18.15e != %18.15e",data.v[0], cc[3]),Math.abs(data.v[0] - cc[3]) < EPS);
        }
    }

    public void testSphereDensity() {

        printf("testSphereDensity()\n");
        double voxelSize = 0.1;
        Sphere sphere = new Sphere(new Vector3d(1,0,0), 1.);
        sphere.setDataType(DataSource.DATA_TYPE_DENSITY);
        sphere.initialize();
        Vec pnt = new Vec(3);
        pnt.setVoxelSize(voxelSize);
        Vec data = new Vec(3);
        double coord[][] = new double[][]{{0,0,0, 0.5},{0.5,0,0, 1},{1.5,0,0, 1},{2.5,0,0, 0.},{voxelSize/2,0,0, 0.75},{-voxelSize/2,0,0, 0.25}};
        
        for(int i = 0; i < coord.length; i++){
            double cc[] = coord[i];
            pnt.v[0] = cc[0];
            pnt.v[1] = cc[1];
            pnt.v[2] = cc[2];
            sphere.getDataValue(pnt, data);
            printf("pnt: [%8.5f  %8.5f  %8.5f] data: %8.5f expect: %8.5f\n", cc[0], cc[1], cc[2], data.v[0], cc[3]);            
            assertTrue(fmt("data.v[0]==cc[3]: %18.15e != %18.15e",data.v[0], cc[3]),Math.abs(data.v[0] - cc[3]) < EPS);
            
        }
    }
  

    static final double EPS = 1.e-12;


    void devTestMakeSlice() throws Exception {

        double r = 8*MM;
        double margin = 8*MM;
        double vs = 1*MM;
        double s = r + margin;
        double distanceBand = 1*MM;

        Sphere sphere = new Sphere(r);

        Bounds bounds = new Bounds(-s,s,-s,s,-s,s);
        AttributeGrid  grid = DevTestUtil.makeDistanceGrid(bounds, vs, s);
        printf("grid: [%d x %d x %d]\n", grid.getWidth(),grid.getHeight(), grid.getDepth());
        GridMaker gm = new GridMaker();
        gm.setSource(sphere);
        gm.makeGrid(grid);

        double su = bounds.getSizeX();
        double sv = bounds.getSizeY();
        int nu = 1000;
        int nv = (int)(nu * sv/su);
        Vector3d eu = new Vector3d(su/nu, 0, 0);
        Vector3d ev = new Vector3d(0, sv/nv, 0);
        Vector3d sliceOrigin = new Vector3d(bounds.xmin, bounds.ymin, bounds.getCenterZ());
        ColorMapper cm = new ColorMapperDistance(distanceBand);
        
        gm.setSource(sphere);
        writeSlice(grid,grid.getDataChannel(), cm, sliceOrigin, eu, ev,nu, nv, "/tmp/00_sphereSliceZ.png");
  
    }


    public static void main(String[] args) throws Exception {
        //new TestSphere().testSphereDistance();
        //new TestSphere().testSphereDensity();
        new TestSphere().devTestMakeSlice();
        
    }

}