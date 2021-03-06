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
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

// Internal Imports

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;

/**
 * Tests the functionality of a Grid2DByte
 *
 * @author Vladimir Bulatov
 * @version
 */
public class TestGridMask extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestGridMask.class);
    }

    public void testSmallGridState(){
        printf("%s.testSmallGrid()\n", this);
        int nx = 10;
        int ny = 20;
        int nz = 20;
        GridMask grid = new GridMask(nx, ny, nz);
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    byte state = (byte)((x*y) & 1);
                    grid.setState(x,y,z,state);
                    byte s = grid.getState(x,y,z);
                    if(s != state) 
                        assertTrue(fmt("%s.setState(%d) != getState(%d, %d, %d) \n",state, s, x, y, z), s == state);
                }
            }
        }
    }

    public void testSmallGridAttribute(){
        printf("%s.testSmallGrid()\n", this);
        int nx = 10;
        int ny = 15;
        int nz = 20;
        GridMask grid = new GridMask(nx, ny, nz);
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    long att = ((x*y) & 1);
                    grid.setAttribute(x,y,z,att);
                    long a = grid.getAttribute(x,y,z);
                    if(a != att) 
                        assertTrue(fmt("%s.setState(%d) != getState(%d, %d, %d) \n",att, a, x, y, z), a == att);
                }
            }
        }
    }

    public static void main(String arg[]){

        new TestGridMask().testSmallGridAttribute();

    }

}
