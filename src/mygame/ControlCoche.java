/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.ai.navmesh.NavMesh;
import com.jme3.ai.navmesh.NavMeshPathfinder;
import com.jme3.ai.navmesh.Path.Waypoint;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResults;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import com.jme3.scene.shape.Box;
import java.io.IOException;

/**
 *
 * @author darylfed
 */
public class ControlCoche extends AbstractControl {

    private BetterCharacterControl playerControl;

    private Node playerNode; // Nodo donde estara el jugador en el mapa

    private Vector3f finalPoint;
    private boolean moving = false;

    private NavMeshPathfinder navi;
    private NavMesh navMesh;
    
    private Geometry player; // Despues se reemplazara al implementar el modelo3d
    private Node bola;

    public ControlCoche(Node playerNode, AssetManager assetManager, BulletAppState bulletAppState, NavMesh navMesh, Node bola) {
        this.playerNode = playerNode;

        // Seteo temporal, deberia de cargar el modelo
        Box c = new Box(2f, 9f, 2f);
        player = new Geometry("Player", c);
        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", new ColorRGBA(0.247f, 0.285f, 0.678f, 1));
        player.setMaterial(material);
        player.setLocalTranslation(0, 3, 0);

        playerNode.attachChild(player);

        playerControl = new BetterCharacterControl(1.5f, 9f, 15);
        playerNode.addControl(playerControl);
        playerControl.setGravity(new Vector3f(0, -10, 0));
        playerControl.setJumpForce(new Vector3f(0, 30, 0));
        playerControl.warp(new Vector3f(0, 2, 0));

        bulletAppState.getPhysicsSpace().add(playerControl);
        bulletAppState.getPhysicsSpace().addAll(playerNode);

        // Crear navigator
        navi = new NavMeshPathfinder(navMesh);
    }
    
    public ControlCoche(){
        System.err.println("Empty ControlCcohe constructor has been called, we should avoid this");
    }

    @Override
    protected void controlUpdate(float tpf) {
        playerControl.setWalkDirection(Vector3f.ZERO);
        navi.setPosition(playerNode.getLocalTranslation());
        navi.computePath(bola.getLocalTranslation());
        
        
        // Movimiento
        if (moving && finalPoint != null) {
            Waypoint wayPoint = navi.getNextWaypoint();

            if (wayPoint != null) {
                Vector3f direccion = wayPoint.getPosition().subtract(playerNode.getLocalTranslation());
                playerControl.setWalkDirection(direccion.normalize().mult(20));
                
                // Settear direccion del coche
                Quaternion directionRot = new Quaternion();
                directionRot.lookAt(direccion.normalize(), Vector3f.UNIT_Y);
                playerNode.setLocalRotation(directionRot);

                if (playerNode.getLocalTranslation().distance(wayPoint.getPosition()) <= 4 && !navi.isAtGoalWaypoint()) {
                    System.out.println("Next waypoint");
                    navi.goToNextWaypoint();
                }

                if (navi.isAtGoalWaypoint()) {
                    System.out.println("AT waypoint");
                    moving = false;
                    navi.clearPath();
                }
            }

        }

        // Disparo
        
        //
        
    }

    public void computeNewPath(Vector3f finalPoint) {
        navi.setPosition(playerNode.getLocalTranslation());
        navi.computePath(finalPoint);
    }

    public BetterCharacterControl getPlayerControl() {
        return playerControl;
    }

    public void setPlayerControl(BetterCharacterControl playerControl) {
        this.playerControl = playerControl;
    }

    public Vector3f getFinalPoint() {
        return finalPoint;
    }

    public void setFinalPoint(Vector3f finalPoint) {
        this.finalPoint = finalPoint;
    }

    public boolean isMoving() {
        return moving;
    }

    public void setMoving(boolean moving) {
        this.moving = moving;
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        //Only needed for rendering-related operations,
        //not called when spatial is culled.
    }

    public Control cloneForSpatial(Spatial spatial) {
        ControlCoche control = new ControlCoche();
        //TODO: copy parameters to new Control
        return control;
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule in = im.getCapsule(this);
        //TODO: load properties of this Control, e.g.
        //this.value = in.readFloat("name", defaultValue);
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule out = ex.getCapsule(this);
        //TODO: save properties of this Control, e.g.
        //out.write(this.value, "name", defaultValue);
    }

}
