package com.jaeheonshim.tankwars;

public class TankInputConfig {
    private  final int DRIVE_FORWARD;
    private final int DRIVE_BACKWARD;

    private final int TURN_LEFT;
    private final int TURN_RIGHT;

    private final int FIRE;

    private final int RELOAD;

    public TankInputConfig(int DRIVE_FORWARD, int DRIVE_BACKWARD, int TURN_LEFT, int TURN_RIGHT, int FIRE, int RELOAD) {
        this.DRIVE_FORWARD = DRIVE_FORWARD;
        this.DRIVE_BACKWARD = DRIVE_BACKWARD;
        this.TURN_LEFT = TURN_LEFT;
        this.TURN_RIGHT = TURN_RIGHT;
        this.FIRE = FIRE;
        this.RELOAD = RELOAD;
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

    public int getReload() {
        return RELOAD;
    }
}
