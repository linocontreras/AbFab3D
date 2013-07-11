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

package abfab3d.grid.op;

import abfab3d.util.Vec;
import abfab3d.util.DataSource;
import abfab3d.util.Initializable;
import abfab3d.util.VecTransform;

/**
   base class for DataSOurces which wnat to be transformable 
 */
public abstract class TransformableDataSource implements DataSource, Initializable {

    protected VecTransform m_transform; 

    protected TransformableDataSource(){
    }
    
    public void setTransform(VecTransform transform){
        m_transform = transform; 
    }
    
    public int initialize(){
        if(m_transform != null && m_transform instanceof Initializable){
            return ((Initializable)m_transform).initialize();
        }

        return RESULT_OK;
    }

    public abstract int getDataValue(Vec pnt, Vec data);


    protected final int transform(Vec pnt){
        if(m_transform != null){
            return m_transform.inverse_transform(pnt, pnt);
        }
        return RESULT_OK;
    }
    

}
