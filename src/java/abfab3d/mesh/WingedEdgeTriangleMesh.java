/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.mesh;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.io.PrintStream;
import java.util.*;


/**
 * 3D Triangle Mesh structure implemented using a WingedEdge datastructure.  Made for easy traversal of edges and dynamic in
 * nature for easy edge collapses.
 *
 * @author Vladimir Bulatov
 * @author Alan Hudson
 */
public class WingedEdgeTriangleMesh {
    private static final boolean DEBUG = true;//false;

    private Face faces;
    private Face lastFace;

    private Vertex vertices;
    private Vertex lastVertex;

    private HashMap<Point3d, Vertex> tvertices = new HashMap<Point3d, Vertex>();

    private Edge edges;
    private Edge lastEdge;
//    private HashMap<HalfEdgeKey, HalfEdge> edgeMap = new HashMap<HalfEdgeKey, HalfEdge>();

    // TODO: Used a linked hashmap to get a consistent order for debugging
    private HashMap<HalfEdgeKey, HalfEdge> edgeMap = new LinkedHashMap<HalfEdgeKey, HalfEdge>();
    private HalfEdgeKey skey = new HalfEdgeKey();

    private int vertexCount = 0;

    /** Scratch vector for computation */
    private Vector3d svec1 = new Vector3d();

    /** Scratch vector for computation */
    private Vector3d svec2 = new Vector3d();

    public WingedEdgeTriangleMesh(Point3d vertCoord[], int[][] findex) {

        Vertex V[] = new Vertex[vertCoord.length];

        for (int nv = 0; nv < V.length; nv++) {

            V[nv] = new Vertex();
            V[nv].setPoint(new Point3d(vertCoord[nv]));
            V[nv].setID(nv);
        }

        ArrayList eface = new ArrayList(findex.length * 3);

        for (int i = 0; i < findex.length; i++) {

            // Create the half edges
            int[] face = findex[i];
            if (face == null) {
                throw new IllegalArgumentException("Face's cannot be null");
            }

            eface.clear();

            if (face.length != 3) {
                throw new IllegalArgumentException("Faces must be triangles.  Index: " + i);
            }
            for (int j = 0; j < face.length; j++) {

                Vertex v1 = V[face[j]];
                Vertex v2 = V[face[(j + 1) % face.length]];
                HalfEdge he = buildHalfEdge(v1, v2);
//                edgeMap.put(new HalfEdgeKey(he.getEnd(), he.getStart()), he);

                // TODO: why is this backwards?
                edgeMap.put(new HalfEdgeKey(he.getStart(), he.getEnd()), he);
                eface.add(he);
            }

            // Create the face
            buildFace(eface);

        }

        boolean notifyNonManifold = true;

        HalfEdgeKey key = new HalfEdgeKey();

        // Find the twins
        for (HalfEdge he1 : edgeMap.values()) {
            if (he1.getTwin() == null) {
                // get halfedge of _opposite_ direction
                //key.setStart(he1.getStart());
                //key.setEnd(he1.getEnd());

                key.setStart(he1.getEnd());
                key.setEnd(he1.getStart());
                HalfEdge he2 = edgeMap.get(key);
                if (he2 != null) {
                    betwin(he1, he2);
                    buildEdge(he1); // create the edge!
                } else {
                    if (DEBUG && notifyNonManifold) {
                        System.out.println("NonManifold hedge: " + he1 + " ? " + he1.getStart().getID() + "->" + he1.getEnd().getID());
                    }
                    // Null twin means its an outer edge on a non-manifold surface
                    buildEdge(he1);
                }
            }
        }

        /* Add the vertices to the list */
        for (int i = 0; i < V.length; i++) {
            addVertex(V[i]);
        }
    }

    /**
     * Get the edges
     *
     * @return A linked list of edges
     */
    public Edge getEdges() {
        return edges;
    }

    public Iterator<Edge> edgeIterator() {
        return new EdgeIterator(edges);
    }

    public Vertex getVertices() {
        return vertices;
    }

    public Iterator<Vertex> vertexIterator() {
        return new VertexIterator(vertices);
    }

    public Face getFaces() {
        return faces;
    }

    public Iterator<Face> faceIterator() {
        return new FaceIterator(faces);
    }

    public Vertex[][] getFaceIndexes() {

        // init vert indices
        initVertexIndices();

        int faceCount = getFaceCount();

        Vertex[][] findex = new Vertex[faceCount][];
        int fcount = 0;

        for (Face f = faces; f != null; f = f.getNext()) {

            Vertex[] face = new Vertex[3];
            findex[fcount++] = face;
            int v = 0;
            HalfEdge he = f.getHe();
            do {
                face[v++] = he.getStart();
                he = he.getNext();
            } while (he != f.getHe());
        }

        return findex;

    }

    /**
     * Get the count of vertices in this mesh.  Local variable kept during upkeep so it's a fast operation.
     *
     * @return The count
     */
    public int getVertexCount() {
        return vertexCount;
    }

    /**
     * Get the count of triangles in this mesh.  Traverses the face list to count so its a relatively
     * slow operation.
     *
     * @return The count
     */
    public int getTriangleCount() {
        Face f = faces;
        Face start = f;
        int cnt = 0;

        while(f != null) {
            cnt++;

            f = f.getNext();

            if (f == start) {
                break;
            }
        }

        return cnt;
    }

    /**
     * Get the count of edges in this mesh.  Traverses the edge list to count so its a relatively
     * slow operation.
     *
     * @return The count
     */
    public int getEdgeCount() {
        Edge e = edges;
        Edge start = e;
        int cnt = 0;

        while(e != null) {
            cnt++;

            e = e.getNext();

            if (e == start) {
                break;
            }
        }

        return cnt;
    }

    /**
     * Collapse an edge.
     *
     * @param e The edge to collapse
     * @param pos The position of the new common vertex
     */
    public void collapseEdge(Edge e, Point3d pos) {
        if (DEBUG) System.out.println("Collapsing edge: " + e + " to pos: " + pos);
        // reuse first vertex as new common vertex
        Vertex commonv = e.getHe().getStart();
        tvertices.remove(commonv.getPoint());
        commonv.getPoint().x = pos.x;
        commonv.getPoint().y = pos.y;
        commonv.getPoint().z = pos.z;
        tvertices.put(commonv.getPoint(), commonv);

        if (DEBUG) System.out.println("Moving vertex: " + commonv.getID() + " to: " + pos);

        // remove deleted faces
        Face face1 = e.getHe().getLeft();
        Face face2 = e.getHe().getTwin().getLeft();

        Vertex removev = e.getHe().getEnd();

        if (DEBUG) System.out.println("Update vertex refs: remove: " + removev.getID() + " to: " + commonv.getID());

        HashSet<Edge> redges = new HashSet<Edge>();

        // Update vertex references to common vertex
        changeVertex(face1, removev, commonv, redges);
        changeVertex(face2, removev, commonv, redges);


        if (DEBUG) System.out.println("Removing vertex: " + removev.getID());
        removeVertex(removev);

        HalfEdge he = face1.getHe();
        do {
            redges.add(he.getEdge());
            he = he.getNext();

        } while (he != face1.getHe());

        he = face2.getHe();
        do {
            redges.add(he.getEdge());
            he = he.getNext();

        } while (he != face2.getHe());

        removeHalfEdges(face1);
        removeHalfEdges(face2);
        removeFace(face1);
        removeFace(face2);
        removeEdge(e);

        // Fix up dangling half edges
        HalfEdge he1;
        HalfEdgeKey key = new HalfEdgeKey();
/*
        System.out.println("Edges Involved");
        for(Edge edge : redges) {
            System.out.println("   " + edge + " hc: " + edge.hashCode());
        }
*/

/*
        // TODO: Debug fixup all edges, why is this necessary?
        System.out.println("Using all edges instead of list");
        redges.clear();
        for(Edge e1 = edges; e1 != null; e1 = e1.getNext()) {
            redges.add(e1);
        }
*/
        if (DEBUG) System.out.println("Checking for degenerate faces:");
        // find and remove any degenerate faces
        for (Edge e1 : redges) {

            he1 = e1.getHe();

            if (he1 == null) {
                continue;
            }

            Face f = he1.getLeft();

            if (isFaceDegenerate(f)) {
                if (DEBUG) {
                    System.out.println("Found degenerate face: " + f);
                }

                removeHalfEdges(f);
                removeFace(f);
            }

            HalfEdge twin = he1.getTwin();

            if (twin == null) {
                continue;
            }
            f = twin.getLeft();

            if (isFaceDegenerate(f)) {
                if (DEBUG) {
                    System.out.println("Found degenerate face: " + f);
                }

                removeHalfEdges(f);
                removeFace(f);
            }
        }

        for (Edge e1 : redges) {

            he1 = e1.getHe();

            if (he1 == null) {
                continue;
            }

            HalfEdge twin = he1.getTwin();

            if (twin == null) {
/*
                key.o1 = he1.getEnd();
                key.o2 = he1.getStart();
                HalfEdge e2 = edgeMap.get(key);
                if (e2 != null) {
                    betwin(he1, e2);
                } else {
                    throw new IllegalArgumentException("Invalid edge");
                }
*/
                key.setStart(he1.getStart());
                key.setEnd(he1.getEnd());
                HalfEdge e2 = edgeMap.get(key);
                if (e2 != null) {
                    betwin(he1, e2);
                } else {
                    writeOBJ(System.out);
                    System.out.println("EdgeMap: ");
                    for(Map.Entry<HalfEdgeKey, HalfEdge> entry : edgeMap.entrySet()) {
                        System.out.println(entry.getKey() + " val: " + entry.getValue());
                    }
                    throw new IllegalArgumentException("Can't find twin for: " + he1 + " o1: " + he1.getStart().getID() + " o2: " + he1.getEnd().getID());

                }
            }
        }


    }

    /**
     * Check if a face is degenerate.  Does it goto burning man etc?  More formally are any two
     * points coincident.
     *
     * @param f The face
     * @return true if the face is degenerate
     */
    private boolean isFaceDegenerate(Face f) {
        Vertex p1;
        Vertex p2;
        Vertex p3;

        p1 = f.getHe().getStart();
        p2 = f.getHe().getEnd();

        HalfEdge he = f.getHe().getNext();

        if (he.getStart() != p1 && he.getStart() != p2) {
            p3 = he.getStart();
        } else if (he.getEnd() != p1 && he.getEnd() != p2) {
            p3 = he.getStart();
        } else {
            // Cannot find a unique third point
            return true;
        }


        svec1.x = p2.getPoint().x - p1.getPoint().x;
        svec1.y = p2.getPoint().y - p1.getPoint().y;
        svec1.z = p2.getPoint().z - p1.getPoint().z;

        svec2.x = p3.getPoint().x - p1.getPoint().x;
        svec2.y = p3.getPoint().y - p1.getPoint().y;
        svec2.z = p3.getPoint().z - p1.getPoint().z;

        svec1.cross(svec1,svec2);
        double area = svec1.length();

        if (DEBUG) System.out.println("Checking: f: " + f.hashCode() + " v: " + p1.getID() + " " + p2.getID() + " " + p3.getID() + " p: " + p1.getPoint() + " p2: " + p2.getPoint() + " p3: " + p3.getPoint() + " area: " + area);

        double EPS = 1e-10;

        if (area < EPS)  {
            return true;
        }
/*

System.out.println("Checking: f: " + f.hashCode() + " v: " + p1.getID() + " " + p2.getID() + " " + p3.getID() + " p: " + p1.getPoint() + " p2: " + p2.getPoint() + " p3: " + p3.getPoint());
        if (p1.getPoint().epsilonEquals(p2.getPoint(), EPS)) {
            return true;
        }
        if (p1.getPoint().epsilonEquals(p3.getPoint(), EPS)) {
            return true;
        }
        if (p2.getPoint().epsilonEquals(p3.getPoint(), EPS)) {
            return true;
        }
*/
        return false;
    }


    /**
     * Change a vertex reference from one vertex to another for a face.
     *
     * @param f The face
     * @param vorig The original vertex
     * @param vnew The new vertex
     * @param hedges List of hald
     */
    private void changeVertex(Face f, Vertex vorig, Vertex vnew, Set<Edge> hedges) {
        if (DEBUG) System.out.println("ChangeVertex on face: " + f + " orig: " + vorig.getID() + " vnew: " + vnew.getID());
        HalfEdge he = f.getHe();
        HalfEdge start = he;

        while(he != null) {
            if (he.getStart() == vorig) {
                if (DEBUG) System.out.print("   Update vertex: " + he);
                hedges.add(he.getEdge());

                // remove old edgeMap entry
                HalfEdgeKey key = new HalfEdgeKey(he.getStart(), he.getEnd());
                edgeMap.remove(key);

                he.setStart(vnew);

                // readd edgeMap entry
                key.setStart(he.getStart());
                key.setEnd(he.getEnd());
                edgeMap.put(key, he);

                if (DEBUG) System.out.println("   to -->: " + he);

                // Recurse into next face to find other vertex
                changeVertex(he.getTwin().getLeft(), vorig, vnew, hedges);
            } else if (he.getEnd() == vorig) {
                if (DEBUG) System.out.print("   Update vertex: " + he);
                hedges.add(he.getEdge());

                // remove old edgeMap entry
                HalfEdgeKey key = new HalfEdgeKey(he.getStart(), he.getEnd());
                edgeMap.remove(key);

                he.setEnd(vnew);

                // readd edgeMap entry
                key.setStart(he.getStart());
                key.setEnd(he.getEnd());
                edgeMap.put(key, he);

                if (DEBUG) System.out.println("   to -->: " + he);

                // Recurse into next face to find other vertex
                changeVertex(he.getTwin().getLeft(), vorig, vnew, hedges);
            }

            he = he.getNext();

            if (he == start) {
                break;
            }
        }
    }

    public void writeOBJ(PrintStream out) {

        Face f;
        Vertex v;
        Edge e;
        int counter = 0;

        for (v = vertices; v != null; v = v.getNext()) {
            out.println("v " + v.getPoint() + " id: " + v.getID());
            v.setID(counter);
            counter++;
        }

        for (f = faces; f != null; f = f.getNext()) {

            out.print("f");
            HalfEdge he = f.getHe();
            do {
                out.print(" " + he.getStart().getID());
                he = he.getNext();
            } while (he != f.getHe());

            out.println(" hc: " + f.hashCode());
        }

        for (e = edges; e != null; e = e.getNext()) {

            out.print("e " + e.getHe().getStart().getID() + " " + e.getHe().getEnd().getID());
            HalfEdge twin = e.getHe().getTwin();
            if (twin != null)
                out.print("  tw: " + twin.getStart().getID() + " " + e.getHe().getTwin().getEnd().getID());
            else
                out.print("  tw: null");

            System.out.println(" hc: " + e.hashCode());
        }
    }

    Vertex buildVertex(Point3d p) {

        Vertex v = new Vertex();
        v.setPoint(p);
        addVertex(v);
        return v;
    }

    void addVertex(Vertex v) {

        //System.out.println("addVertex: " + v.p);
        // is the list empty?
        if (vertices == null) {

            lastVertex = v;
            vertices = v;

        } else {

            lastVertex.setNext(v);
            lastVertex = v;

        }

        v.setID(vertexCount++);

        //System.out.println("index: " + v.index);

        tvertices.put(v.getPoint(), v);

    }

    public void removeVertex(Vertex v) {

        Vertex prev = getPreviousVertex(v);
        if (prev != null) {
            prev.setNext(v.getNext());
            if (v == lastVertex) {
                lastVertex = prev;
                lastVertex.setNext(null);
            }
        }


        tvertices.remove(v);
        vertexCount--;
    }


    Face buildFace(HalfEdge hedges[]) {

        Face f = new Face();

        f.setHe((HalfEdge) hedges[0]);

        int size = hedges.length;
        for (int e = 0; e < size; e++) {

            HalfEdge he1 = hedges[e];
            he1.setLeft(f);
            HalfEdge he2 = (HalfEdge) hedges[(e + 1) % size];
            joinHalfEdges(he1, he2);

        }

        addFace(f);

        return f;

    }


    Face buildFaceV(List<Vertex> vert) {

        HalfEdge edges[] = new HalfEdge[vert.size()];

        int size = edges.length;

        for (int v = 0; v < size; v++) {

            Vertex v1 = (Vertex) vert.get(v);
            Vertex v2 = (Vertex) vert.get((v + 1) % size);
            edges[v] = buildHalfEdge(v1, v2);
        }

        return buildFace(edges);

    }

    Face buildFaceV(Vertex[] vert) {

        HalfEdge edges[] = new HalfEdge[vert.length];

        int size = edges.length;

        for (int v = 0; v < size; v++) {

            Vertex v1 = vert[v];
            Vertex v2 = vert[(v + 1) % size];
            edges[v] = buildHalfEdge(v1, v2);
        }

        return buildFace(edges);

    }

    /**
     *
     *
     *
     */
    public HalfEdge getTwin(Vertex head, Vertex tail) {

        //System.out.println("getTwin() " + head.index + "-" + tail.index);
        // return halfedge, which has corresponding head and tail
        HalfEdge he = head.getLink();
        do {
            //System.out.println(" ?twin? " + he);
            if ((he.getStart() == head) && (he.getEnd() == tail)) {
                return he;
            }
            he = he.getNext();
        } while (he != head.getLink());

        return null;
    }

    /**
     *
     *
     *
     */
    public Face addNewFace(Point3d coord[]) {

        Vertex vert[] = new Vertex[coord.length];

        for (int i = 0; i < vert.length; i++) {

            vert[i] = findVertex(coord[i]);
            if (vert[i] == null)
                vert[i] = buildVertex(coord[i]);
        }

        return addNewFace(vert);

    }


    /**
     *
     *
     *
     */
    public Face addNewFace(Vertex vert[]) {

        Face face = buildFaceV(vert);

        HalfEdge he = face.getHe();

        do {

            HalfEdge twin = getTwin(he.getEnd(), he.getStart());
            //System.out.println("he:" + he + " twin: " + twin);
            if (twin != null) {
                betwin(he, twin);
                buildEdge(he); // create the edge!
            }
            he = he.getNext();
        } while (he != face.getHe());


        return face;

    }


    Face buildFace(List<HalfEdge> vhedges) {

        Face f = new Face();

        f.setHe(vhedges.get(0));

        int size = vhedges.size();
        for (int e = 0; e < size; e++) {

            HalfEdge he1 = vhedges.get(e);
            he1.setLeft(f);

            HalfEdge he2 = vhedges.get((e + 1) % size);
            joinHalfEdges(he1, he2);

        }

        addFace(f);

        return f;

    }

    void joinHalfEdges(HalfEdge he1, HalfEdge he2) {

        he1.setNext(he2);
        he2.setPrev(he1);

    }


    public Vertex getPreviousVertex(Vertex vert) {

        for (Vertex v = vertices; v != null; v = v.getNext()) {

            if (v.getNext() == vert)
                return v;
        }

        return null;

    }

    /**
     * Remove a face from the mesh.  This method does not remove any edges or vertices.
     *
     * @param f The face to remove
     */
    public void removeFace(Face f) {

        if (DEBUG) {
            System.out.println("Removing face: " + f + " hc: " + f.hashCode());
        }
        Face prev = f.getPrev();

        if (prev != null) {
            prev.setNext(f.getNext());
        } else {
            // updating head
            faces = f.getNext();
        }
        f.getNext().setPrev(f.getPrev());

        if (f == lastFace) {
            lastFace = prev;
            lastFace.setNext(null);
        }

    }

    /**
     * Remove the half edges making up a face.
     *
     * @param f The face to remove
     */
    public void removeHalfEdges(Face f) {

        if (DEBUG) System.out.println("Removing half edges for face: " + f);
        HalfEdge he = f.getHe();
        do {

            removeHalfEdge(he);

            he.setPrev(null);
            he = he.getNext();

        } while (he != f.getHe());

    }

    /**
     * Remove an edge.  Does not remove any vertices or other related structures.
     *
     * @param e The edge to remove
     */
    public void removeEdge(Edge e) {

        if (DEBUG) System.out.println("removeEdge: " + e);

        Edge prev = e.getPrev();

        if (prev != null) {
            prev.setNext(e.getNext());
        } else {
            edges = e.getNext();
        }
        e.getNext().setPrev(e.getPrev());

        if (e == lastEdge) {
            lastEdge = prev;
            lastEdge.setNext(null);
        }

    }

    void removeHalfEdge(HalfEdge he) {

        if (DEBUG) System.out.println("removeHalfEdge()" + he);

        HalfEdge twin = he.getTwin();
        Edge e = he.getEdge();

        if (e.getHe() == he) {
            // this he is at the head of he list
            // place another one at the head
            e.setHe(he.getTwin());
        }

        if (twin == null) {
            removeEdge(e);

        } else {
if (DEBUG) System.out.println("Clearing twin: " + twin);
            twin.setTwin(null);
        }

        // TODO: causes problems, not sure why
        //System.out.println("Removing edgeMap: " + he);
        //edgeMap.remove(new HalfEdgeKey(he.getStart(), he.getEnd()));
    }

    private void addFace(Face f) {

        /* is the list empty? */
        if (faces == null) {

            lastFace = f;
            faces = f;

        } else {

            lastFace.setNext(f);
            f.setPrev(lastFace);
            lastFace = f;
        }

        //System.out.println("addFace(): " + f.next);

    }

    private void addEdge(Edge e) {

        /* is the list empty? */
        if (edges == null) {
            lastEdge = e;
            edges = e;
        } else {
            lastEdge.setNext(e);
            e.setPrev(lastEdge);
            lastEdge = e;
        }
    }

    private HalfEdge buildHalfEdge(Vertex tail, Vertex head) {

        HalfEdge he = new HalfEdge();

        he.setEnd(tail);
        if (tail.getLink() == null)
            tail.setLink(he); // link the tail vertex to this edge
        he.setStart(head);
        return he;

    }

    Edge buildEdge(HalfEdge he) {

        Edge e = new Edge();
        e.setHe(he);
        he.setEdge(e);

        if (he.getTwin() != null) {
            he.getTwin().setEdge(e);
        }

        addEdge(e);

        return e;

    }

    void betwin(HalfEdge he1, HalfEdge he2) {

        he1.setTwin(he2);
        he2.setTwin(he1);

    }

    int getFaceCount() {

        int count = 0;
        Face f = faces;

        while (f != null) {

            count++;
            f = f.getNext();

        }
        return count;
    }

    int initVertexIndices() {

        int vc = 0;
        Vertex v = vertices;
        while (v != null) {
            v.setID(vc++);
            v = v.getNext();
        }
        return vc;

    }

    /**
     * Find a vertex using a Point3D value epsilon.
     * @param p
     * @return
     */
    public Vertex findVertex(Point3d p, double eps) {

        Vertex v = vertices;
        while (v != null) {
            if (v.getPoint().distanceSquared(p) < eps) {
                return v;
            }
            v = v.getNext();
        }
        return null;
    }

    /**
     * Find a vertex using a point3d reference.
     *
     * @param v
     * @return
     */
    public Vertex findVertex(Point3d v) {

        return (Vertex) tvertices.get(v);

    }
}
