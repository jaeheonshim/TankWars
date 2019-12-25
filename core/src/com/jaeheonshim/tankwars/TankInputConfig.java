package com.jaeheonshim.tankwars;

public class TankInputConfig {
    public final int DRIVE_FORWARD;
    public final int DRIVE_BACKWARD;

    public final int TURN_LEFT;
    public final int TURN_RIGHT;

    public final int FIRE;

    public TankInputConfig(int DRIVE_FORWARD, int DRIVE_BACKWARD, int TURN_LEFT, int TURN_RIGHT, int FIRE) {
        this.DRIVE_FORWARD = DRIVE_FORWARD;
        this.DRIVE_BACKWARD = DRIVE_BACKWARD;
        this.TURN_LEFT = TURN_LEFT;
        this.TURN_RIGHT = TURN_RIGHT;
        this.FIRE = FIRE;
    }

    public int getDriveForward() {
        return DRIVE_FORWARD;
    }

    public int getDriveBackward() {
        return DRIVE_BACKWARD;
    }

    public int getTurnLeft() {
        return TURN_LEFT;
    }

    public int getTurnRight() {
        return TURN_RIGHT;
    }

    public int getFire() {
        return FIRE;
    }
}
