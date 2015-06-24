/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2015
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package abfab3d.opencl;

import com.jogamp.opencl.CLResource;

/**
 * A resource wrapper for a JOGL CLResource
 *
 * @author Alan Hudson
 */
public class OpenCLResource implements Resource {
    private CLResource resource;

    public OpenCLResource(CLResource resource) {
        this.resource = resource;
    }

    @Override
    public void release() {
        resource.release();
    }

    public CLResource getResource() {
        return resource;
    }
}