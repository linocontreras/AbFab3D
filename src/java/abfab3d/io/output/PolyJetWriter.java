/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011-2018
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fxi it.
 *
 ****************************************************************************/

package abfab3d.io.output;

import javax.vecmath.Vector3d;
import java.util.Hashtable;
import java.util.ArrayList;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;


// external imports


// Internal Imports
import abfab3d.core.Vec;
import abfab3d.core.Color;
import abfab3d.core.DataSource;
import abfab3d.core.Bounds;
import abfab3d.core.Initializable;
import abfab3d.core.MathUtil;

import abfab3d.param.Parameter;
import abfab3d.param.SNodeParameter;
import abfab3d.param.DoubleParameter;
import abfab3d.param.IntParameter;
import abfab3d.param.StringParameter;
import abfab3d.param.BaseParameterizable;
import abfab3d.param.StringListParameter;



import abfab3d.util.ImageUtil;


import abfab3d.grid.op.ImageLoader;
import abfab3d.datasources.ImageColorMap;


import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.time;
import static abfab3d.core.Units.MM;
import static abfab3d.core.Units.IN;

import static abfab3d.core.MathUtil.clamp;
import static abfab3d.util.ImageUtil.makeARGB;

import static java.lang.Math.sqrt;

/**
   class to export DataSource into slices for PolyJet
*/
public class PolyJetWriter extends BaseParameterizable {

    static final boolean DEBUG = true;

    // named PolyJet materials 
    public static final String S_WHITE = "VeroPureWht";
    public static final String S_BLACK = "VeroBlack";
    public static final String S_CYAN = "VeroCyan";
    public static final String S_YELLOW = "VeroYellow";
    public static final String S_MAGENTA = "VeroMgnt";
    public static final String S_CLEAR = "VeroClear";

    public static final int C_WHITE[] = {240, 240, 240, 255};
    public static final int C_BLACK[] = {26,  26,  29,  255};
    public static final int C_CYAN[] = {0,    90, 158, 255};
    public static final int C_MAGENTA[] = {166,  33,  98, 255};
    public static final int C_YELLOW[] = {200, 189,   3, 255};
    public static final int C_CLEAR[] = {227, 233, 253,  50};

    static int icolors_phys[][] = new int[][]{
        C_CYAN, 
        C_MAGENTA,
        C_YELLOW,
        C_BLACK,
        C_WHITE,
        C_CLEAR,
    };

    // colors of physical materials 
    public static final int I_WHITE = makeARGB(C_WHITE);
    public static final int I_BLACK = makeARGB(C_BLACK);
    public static final int I_CYAN = makeARGB(C_CYAN);
    public static final int I_MAGENTA = makeARGB(C_MAGENTA);
    public static final int I_YELLOW = makeARGB(C_YELLOW);
    public static final int I_CLEAR = makeARGB(C_CLEAR);    
    public static final int I_ERROR = makeARGB(255,0,0,255);
   
    public static final String[] sm_smaterials = {S_WHITE,S_BLACK,S_CYAN,S_YELLOW,S_MAGENTA,S_CLEAR};
    public static final int[] sm_imaterials = {I_WHITE,I_BLACK,I_CYAN,I_YELLOW,I_MAGENTA,I_CLEAR};

    int MAX_CHANNELS=7;

    static final double SLICE_THICKNESS_HR =0.014*MM;
    static final double PIXEL_SIZE_X = IN/600;
    static final double PIXEL_SIZE_Y = IN/300;

    static final Hashtable<String,Integer> sm_materialsTable = new Hashtable<String,Integer>();
    static {
        for(int i = 0; i < sm_imaterials.length; i++){
            sm_materialsTable.put(sm_smaterials[i], new Integer(sm_imaterials[i]));
        }
    }

    SNodeParameter mp_model = new SNodeParameter("model");
    DoubleParameter mp_xmin = new DoubleParameter("xmin", "xmin", 0);
    DoubleParameter mp_xmax = new DoubleParameter("xmax", "xmax", 0);
    DoubleParameter mp_ymin = new DoubleParameter("ymin", "ymin", 0);
    DoubleParameter mp_ymax = new DoubleParameter("ymax", "ymax", 0);
    DoubleParameter mp_zmin = new DoubleParameter("zmin", "zmin", 0);
    DoubleParameter mp_zmax = new DoubleParameter("zmax", "zmax", 0);

    StringListParameter mp_materials = new StringListParameter("materials",new String[]{S_WHITE});
    IntParameter mp_ditheringType = new IntParameter("ditheringType", 0);
    IntParameter mp_firstSlice = new IntParameter("firstSlice", -1);
    IntParameter mp_slicesCount = new IntParameter("slicesCount", -1);
    StringParameter mp_outFolder = new StringParameter("outFolder","/tmp/polyjet");
    StringParameter mp_outPrefix = new StringParameter("outPrefix","slice");

    
    Parameter m_aparam[] = new Parameter[]{

        mp_model,
        mp_xmin, 
        mp_xmax, 
        mp_ymin, 
        mp_ymax, 
        mp_zmin, 
        mp_zmax,
        mp_outFolder,
        mp_outPrefix,
        mp_materials,
        mp_ditheringType,
        mp_firstSlice,
        mp_slicesCount,
    };

    public PolyJetWriter(){
        super.addParams(m_aparam);
    }
    
    Bounds getBounds(){
        
        return new Bounds(mp_xmin.getValue(),mp_xmax.getValue(),
                          mp_ymin.getValue(),mp_ymax.getValue(),
                          mp_zmin.getValue(),mp_zmax.getValue());
    }


    public void setBounds(Bounds bounds) {

        mp_xmin.setValue(bounds.xmin);
        mp_xmax.setValue(bounds.xmax);
        mp_ymin.setValue(bounds.ymin);
        mp_ymax.setValue(bounds.ymax);
        mp_zmin.setValue(bounds.zmin);
        mp_zmax.setValue(bounds.zmax);

    }

    public void setModel(DataSource model) {

        mp_model.setValue(model);
        if (model instanceof Initializable) {
            ((Initializable) model).initialize();
        }
    }
    

    // local member initilized before writing 
    Bounds m_bounds;
    double m_sliceThickness, m_vsx, m_vsy;
    int m_nx, m_ny, m_nz;
    DataSource m_model;
    int m_outsideColor = 0xFF000000;
    int m_materialColors[] = {I_WHITE};
    int m_materialCount = 1;
    double m_materialValues[][];
     

    int m_ditheringType = DITHERING_FLOYD_STEINBERG;

    static final int DITHERING_NONE = -1;
    static final int DITHERING_FLOYD_STEINBERG = 0;
    static final int DITHERING_X_AXIS = 1;
    static final int DITHERING_Y_AXIS = 2;
    static final int DITHERING_XY = 3;

    public void write(){
        
        String outFolder = mp_outFolder.getValue();
        String outPrefix = mp_outPrefix.getValue();
        m_model = (DataSource)(mp_model.getValue());
        m_materialColors = getMaterialsColors(mp_materials.getList());
        m_materialCount = m_materialColors.length;
        m_materialValues = getMaterialValues(m_materialCount);
        m_ditheringType = mp_ditheringType.getValue();
        MathUtil.initialize(m_model);

        m_bounds = getBounds();
        long t0 = time();

        m_sliceThickness = SLICE_THICKNESS_HR;
        m_vsx = PIXEL_SIZE_X;
        m_vsy = PIXEL_SIZE_Y;

        m_nz = m_bounds.getGridDepth(m_sliceThickness);
        m_nx = m_bounds.getGridWidth(m_vsx);
        m_ny = m_bounds.getGridHeight(m_vsy);
        double sliceData[] = new double[m_nx*m_ny*m_materialCount];

        if(m_nx <=0 || m_ny <= 0 || m_nz <= 0){
            throw new RuntimeException(fmt("PolyJewtWriter: illegal output bounds:%s", m_bounds.toString()));
        }
        
        if(DEBUG) {
            printf("PolyJetWriter write()\n");
            printf("              outFolder: %s\n", outFolder);
            printf("              grid: [%d x %d x %d]\n", m_nx, m_ny, m_nz);
        }

        BufferedImage image =  new BufferedImage(m_nx, m_ny, BufferedImage.TYPE_INT_ARGB);
        DataBufferInt db = (DataBufferInt)image.getRaster().getDataBuffer();
        int[] imageData = db.getData();
        int firstSlice = mp_firstSlice.getValue();
        if(firstSlice < 0) firstSlice = 0;
        int slicesCount = mp_slicesCount.getValue();
        if(slicesCount <= 0) slicesCount = m_nz;
        
        for(int iz = firstSlice; iz < slicesCount; iz++){
            //for(int iz = 0; iz < m_nz; iz++){
            calculateSlice(iz, sliceData);
            makeImage(sliceData, imageData); 
            String outPath = fmt("%s/%s_%d.png", outFolder, outPrefix, iz);
            try {
                ImageIO.write(image, "png", new File(outPath));
            } catch(Exception e){
                throw new RuntimeException(fmt("exception while writing to %s", outPath));
            }
        }
        if(DEBUG){
            printf("PolyJetWriter write() done %d ms\n", (time()-t0));
        }
    }

    protected void calculateSlice(int iz, double sliceData[]){
        
        //if(DEBUG)printf("calculateSlice(%d)\n", iz);

        double z = m_bounds.zmin + m_sliceThickness*(iz+0.5);
        double xmin = m_bounds.xmin + m_vsx/2;
        double ymin = m_bounds.ymin + m_vsy/2;
        Vec pnt = new Vec(3);
        Vec data = new Vec(MAX_CHANNELS);
        
        for(int iy = 0; iy < m_ny; iy++){
            double y = ymin + iy*m_vsy;
            for(int ix = 0; ix < m_nx; ix++){
                double x = xmin + ix*m_vsx;
                pnt.set(x,y,z);
                m_model.getDataValue(pnt, data);
                normalizeVoxel(data);
                data.get(sliceData, voxelOffset(ix, iy), m_materialCount);
            }
        }

        
    }

    protected void makeImage(double sliceData[], int imageData[]){
        
        Vec voxel = new Vec(m_materialCount);
        double error[] = new double[m_materialCount];

        double voxelError[] = new double[m_materialCount];

        for(int iy = 0; iy < m_ny; iy++){
            
            
            for(int ix = 0; ix < m_nx; ix++){

                int imgOffset = (ix + (m_ny-1-iy)*m_nx);
                getVoxel(sliceData, ix, iy, voxel);
                
                if(voxel.v[0] > 0) {
                    // outside 
                    imageData[imgOffset] = m_outsideColor;
                    
                } else {      
                    // interior 
                    //addError(voxel, voxelError);
                    int materialIndex = findClosestMaterial(voxel);
                    getError(voxel.v, m_materialValues[materialIndex], voxelError);
                    distributeError(sliceData, ix, iy, voxelError);
                    imageData[imgOffset] = m_materialColors[materialIndex];
                    
                }                         
            }
        }
        //if(DEBUG)printf("writeSlice(%s)\n", path);
        
    }

    void distributeError(double sliceData[], int ix, int iy, double voxelError[]){
        
        switch(m_ditheringType){
        case DITHERING_NONE:
            break;
        case DITHERING_FLOYD_STEINBERG:
            // floyd steinberg
            distributeVoxelError(sliceData, ix+1, iy,   voxelError, 7/16.);
            distributeVoxelError(sliceData, ix, iy+1, voxelError,   5/16.);
            distributeVoxelError(sliceData, ix+1, iy+1, voxelError, 1/16.);
            distributeVoxelError(sliceData, ix-1, iy+1, voxelError, 3/16.);
            break;                        
        case DITHERING_X_AXIS:
            distributeVoxelError(sliceData, ix+1, iy,   voxelError, 1.);
            break;
        case DITHERING_Y_AXIS:
            distributeVoxelError(sliceData, ix, iy+1,   voxelError, 1.);
            break;
        case DITHERING_XY:
            distributeVoxelError(sliceData, ix+1, iy,   voxelError, 0.5);
            distributeVoxelError(sliceData, ix, iy+1,   voxelError, 0.3);
            distributeVoxelError(sliceData, ix+1, iy+1,   voxelError, 0.2);
            break;
        }
    }
        

    void getVoxel(double data[], int ix, int iy, Vec voxel){
        voxel.set(data, voxelOffset(ix, iy));        
    }

    final int voxelOffset(int ix, int iy){
        return (ix + iy*m_nx)*m_materialCount;
    }
    static int[] getMaterialsColors(ArrayList materialNames){
        int colors[] = new int[materialNames.size()];
        for(int i = 0; i < materialNames.size(); i++){
            
            Integer value = sm_materialsTable.get((String)materialNames.get(i));
            if(value != null) colors[i] = value.intValue();
            else colors[i] = I_ERROR;                        
        }

        return colors;
    }

    /**
       return aray of values assigned to voxel of specific material        
     */
    double[][] getMaterialValues(int count){
        
        double mat[][] = new double[count][count];
        for(int i = 0; i < count; i++){
            mat[i][i] = 1.0;
        }
        return mat;
    }


    /**
       return index of material which is closes to the given voxel value 
       voxel.v[0] is signed distance to the surface 
       voxel.v[1] - density of material 1 
       voxel.v[2] - density of material 2
       ...
       voxel.v[m_materialCount-1] - density of last material 

       desnsity of background material (material 0) is calculated as 1.0 - (voxel.v[1] + voxel.v[2] + voxel.v[m_materialCount-1])
       
       each density should be inside of interval [0,1]
       
     */
    int findClosestMaterial(Vec voxel){

        double maxDensity = 0;
        int maxIndex = 0;
        for(int c = 1; c < m_materialCount; c++){ 
            double density = voxel.v[c];
            if(density > maxDensity ) {
                maxDensity = density;
                maxIndex = c;
            }
        }
        if(maxDensity >= 0.5){
            // closest material 
            return maxIndex;
        } else {
            // backgrond material is the closest 
            return 0;
        }            
    }
 
    void addError(Vec voxel, double error[]){
        for(int i = 1; i < m_materialCount; i++){
            voxel.v[i] += error[i];
        }
    }

    boolean isInside(int ix, int iy){
        return (ix < m_nx) && (ix >= 0) && (iy < m_ny) && (iy >= 0);
    }

    void distributeVoxelError(double sliceData[], int ix, int iy, double error[], double weight){

        if(!isInside(ix, iy))
            return;

        int offset = voxelOffset(ix, iy);
        for(int i = 1; i < m_materialCount; i++){
            sliceData[offset + i] += error[i]*weight;
        }
    }

    /**
       makes sure that densities of materials are withing interval[0,1]
     */
    void normalizeVoxel(Vec voxel){

        double remainingDensity = 1;
        double t = 0;
        for(int i = 1; i < m_materialCount; i++){
            t = clamp(voxel.v[i],0, remainingDensity);
            voxel.v[i] = t;
            remainingDensity -= t;
        }
    }

    /**
       return error which result from using realValue instead of requestedValue
     */
    void getError(double requestedValue[], double usedValue[], double error[]){

        for(int i = 1; i < m_materialCount; i++){

            error[i] = requestedValue[i] - usedValue[i];

        }       
    }
    
}