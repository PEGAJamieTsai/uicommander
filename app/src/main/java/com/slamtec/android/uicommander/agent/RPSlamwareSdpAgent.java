package com.slamtec.android.uicommander.agent;

import android.graphics.RectF;

import com.slamtec.android.uicommander.data.MapDataCache;
import com.slamtec.android.uicommander.events.ConnectedEvent;
import com.slamtec.android.uicommander.events.ConnectionLostEvent;
import com.slamtec.android.uicommander.events.LaserScanUpdateEvent;
import com.slamtec.android.uicommander.events.MapUpdateEvent;
import com.slamtec.android.uicommander.events.MoveActionUpdateEvent;
import com.slamtec.android.uicommander.events.RobotPoseUpdateEvent;
import com.slamtec.android.uicommander.events.RobotStatusUpdateEvent;
import com.slamtec.android.uicommander.events.WallUpdateEvent;
import com.slamtec.slamware.AbstractSlamwarePlatform;
import com.slamtec.slamware.action.IMoveAction;
import com.slamtec.slamware.action.MoveDirection;
import com.slamtec.slamware.action.Path;
import com.slamtec.slamware.discovery.DeviceManager;
import com.slamtec.slamware.geometry.Line;
//import com.slamtec.slamware.robot.HealthInfo;
import com.slamtec.slamware.robot.LaserScan;
import com.slamtec.slamware.robot.Location;
import com.slamtec.slamware.robot.Map;
import com.slamtec.slamware.robot.MapKind;
import com.slamtec.slamware.robot.MapType;
import com.slamtec.slamware.robot.Pose;
import com.slamtec.slamware.robot.Rotation;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Vector;


/**
 * Created by Alan on 10/12/15.
 */
public class RPSlamwareSdpAgent {
    private final static String TAG = "RPSlamwareSdpAgent";

    private final static float SLAMWARE_MAX_UPDATE_MAP_RADIX = 7.0f;

    private AbstractSlamwarePlatform robotPlatform_;

    private MapDataCache mapData_;
    private MapDataCache sweepMapData_;

    private Pose robotPose_;
    private LaserScan laserScan_;
    private IMoveAction moveAction_;
    private Vector<Line> walls_;
    private int wallId_;

    private MapType mapType_;
    private RectF mergedMapUpdateArea_;

    private Rotation targetRotation_;
    private Vector<Line> wallsToAdd_;

    private String sdpVersion_;

    private boolean updatingPose_;
    private boolean updatingStatus_;
    private boolean updatingLaserScan_;
    private boolean updatingMoveAction_;
    private boolean updatingWalls_;
    private boolean rotating_;

    private String ip_;
    private int port_;
    private Location location_;
    private MoveDirection direction_;

    private boolean shouldReadLocalizationQuliaty = false;

//    private ExecutorService executorService = Executors.newFixedThreadPool(10);
//
//    private ArrayList<Runnable> jobQueue;
//
//    private Thread workingThread;
//
//    private Runnable workingRunnable = () -> {
//        while (true) {
//            Vector<Runnable> jobs;
//            synchronized (RPSlamwareSdpAgent.this) {
//                jobs = new Vector<>(jobQueue);
//                jobQueue.clear();
//            }
//
//            if (jobs.isEmpty()) {
//                synchronized (RPSlamwareSdpAgent.this) {
//                    workingThread.interrupt();
//                    workingThread = null;
//                }
//                return;
//            }
//
//            for (Runnable job : jobs) {
//                executorService.submit(job);
//            }
//        }
//    };
    private Worker worker;

    private static boolean connected_;

    private static JobConnect jobConnect;
    private static JobAddWalls jobAddWalls;
    private static JobCancelAllActions jobCancelAllActions;
    private static JobClearMap jobClearMap;
    private static JobDisconnect jobDisconnect;
    private static JobGoHome jobGoHome;
    private static JobClearWalls jobClearWalls;
    private static JobMoveBy jobMoveBy;
    private static JobMoveTo jobMoveTo;
    private static JobSweepSpot jobSweepSpot;
    private static JobRemoveWallById jobRemoveWallById;
    private static JobRotateTo jobRotateTo;
    private static JobStartSweep jobStartSweep;
    private static JobUpdateLaserScan jobUpdateLaserScan;
    private static JobUpdateMap jobUpdateMap;
    private static JobUpdateMoveAction jobUpdateMoveAction;
    private static JobUpdatePose jobUpdatePose;
    private static JobUpdateStatus jobUpdateStatus;
    private static JobUpdateWalls jobUpdateWalls;
    private static JobUpdateWholeMap jobUpdateWholeMap;
    private static JobGetRobotHealth jobGetRobotHealth;

    public RPSlamwareSdpAgent() {
        mapData_ = new MapDataCache();
        sweepMapData_ = new MapDataCache();

        mergedMapUpdateArea_ = new RectF(0, 0, 0, 0);
        robotPose_ = new Pose(new Location(0, 0, 0), new Rotation(0, 0, 0));
        port_ = 0;
        updatingPose_ = false;
        updatingStatus_ = false;
        updatingLaserScan_ = false;
        updatingMoveAction_ = false;
        updatingWalls_ = false;
        rotating_ = false;

        wallsToAdd_ = new Vector<>();

        sdpVersion_ = "";

        jobConnect = new JobConnect();
        jobAddWalls = new JobAddWalls();
        jobCancelAllActions = new JobCancelAllActions();
        jobClearMap = new JobClearMap();
        jobDisconnect = new JobDisconnect();
        jobGoHome = new JobGoHome();
        jobClearWalls = new JobClearWalls();
        jobMoveBy = new JobMoveBy();
        jobMoveTo = new JobMoveTo();
        jobSweepSpot = new JobSweepSpot();
        jobRemoveWallById = new JobRemoveWallById();
        jobRotateTo = new JobRotateTo();
        jobStartSweep = new JobStartSweep();
        jobUpdateLaserScan = new JobUpdateLaserScan();
        jobUpdateMap = new JobUpdateMap();
        jobUpdateMoveAction = new JobUpdateMoveAction();
        jobUpdatePose = new JobUpdatePose();
        jobUpdateStatus = new JobUpdateStatus();
        jobUpdateWalls = new JobUpdateWalls();
        jobUpdateWholeMap = new JobUpdateWholeMap();
        jobGetRobotHealth = new JobGetRobotHealth();

//        jobQueue = new ArrayList<>();
        worker = new Worker();
    }

    public MapDataCache getMapData() {
        return mapData_;
    }

    public MapDataCache getSweepMapData() {
        return sweepMapData_;
    }

    public boolean isConnected() {
        return connected_;
    }

    // robot action info
    public synchronized Pose getRobotPose() {
        return robotPose_;
    }

    public synchronized LaserScan getLaserScan() {
        return laserScan_;
    }

    public synchronized IMoveAction getMoveAction() {
        return moveAction_;
    }

    public synchronized Vector<Line> getWalls() {
        return walls_;
    }

    public String getSDPVersion() {
        return sdpVersion_;
    }

    // connection
    public void connectTo(String ip, int port) {
        this.ip_ = ip;
        this.port_ = port;
        pushJob(jobConnect);
    }

    public void reconnect() {
        String ip;
        int port;

        synchronized (this) {
            ip = this.ip_;
            port = this.port_;
        }

        if (ip.isEmpty() || port == 0) {
            return;
        }
        connectTo(ip, port);
    }

    public void disconnect() {
        pushJob(jobDisconnect);
    }


    // robot map info
    public void updateMap(RectF area) {
        if (area.isEmpty()) {
            return;
        }

        synchronized (this) {
            if (mergedMapUpdateArea_ != null && !mergedMapUpdateArea_.isEmpty()) {
                mergedMapUpdateArea_.union(area);
                return;
            } else {
                mergedMapUpdateArea_ = area;
            }
        }

        pushJob(jobUpdateMap);
    }

    public void updateMap() {
        pushJob(jobUpdateWholeMap);
    }

    public void updatePose() {
        synchronized (this) {
            if (updatingPose_) {
                return;
            }

            updatingPose_ = true;
        }

        pushJob(jobUpdatePose);
    }

    public void updateLaserScan() {
        synchronized (RPSlamwareSdpAgent.this) {
            if (updatingLaserScan_) {
                return;
            }

            updatingLaserScan_ = true;
        }

        pushJob(jobUpdateLaserScan);
    }

    public void updateRobotStatus() {
        synchronized (RPSlamwareSdpAgent.this) {
            if (updatingStatus_)
                return;
            updatingStatus_ = true;
        }
        pushJob(jobUpdateStatus);
    }

    public void updateMoveAction() {
        synchronized (RPSlamwareSdpAgent.this) {
            if (updatingMoveAction_)
                return;

            updatingMoveAction_ = true;
        }

        pushJob(jobUpdateMoveAction);
    }

    // robot move action command
    public void moveTo(Location location) {
        synchronized (this) {
            this.location_ = location;
        }
        pushJobHead(jobMoveTo);
    }

    public void rotateTo(Rotation rotation) {
        synchronized (RPSlamwareSdpAgent.this) {
            targetRotation_ = rotation;
            if (rotating_)
                return;
            rotating_ = true;
        }
        pushJob(jobRotateTo);
    }

    public void moveBy(MoveDirection direction) {
        synchronized (this) {
            this.direction_ = direction;
        }
        pushJob(jobMoveBy);
    }

    public void sweepSpot(Location location) {
        synchronized (this) {
            this.location_ = location;
        }
        pushJobHead(jobSweepSpot);
    }

    // clear map
    public void clearMap() {
        pushJobHead(jobClearMap);
    }

    // robot map command
    public void updateWalls() {
        synchronized (RPSlamwareSdpAgent.this) {
            if (updatingWalls_)
                return;
            updatingWalls_ = true;
        }
        pushJob(jobUpdateWalls);
    }

    public void addWall(Line line) {
        synchronized (this) {
            wallsToAdd_.add(line);
            if (wallsToAdd_.size() != 1)
                return;
        }
        pushJob(jobAddWalls);
    }

    public void addWalls(Vector<Line> lines) {
        synchronized (RPSlamwareSdpAgent.this) {
            if (wallsToAdd_.isEmpty()) {
                wallsToAdd_ = lines;
            } else {
                wallsToAdd_.addAll(lines);
                return;
            }
        }
        pushJob(jobAddWalls);
    }

    public void clearWalls() {
        pushJob(jobClearWalls);
    }

    public void removeWallById(int wallId) {
        synchronized (this) {
            wallId_ = wallId;
        }
        pushJob(jobRemoveWallById);
    }

    public void startSweep() {
        pushJobHead(jobStartSweep);
    }

    public void goHome() {
        pushJobHead(jobGoHome);
    }

    public void cancelAllActions() {
        pushJobHead(jobCancelAllActions);
    }

    public void getRobotHealth() {
        pushJob(jobGetRobotHealth);
    }

    private synchronized void pushJobHead(Runnable job) {
//        jobQueue.add(0, job);
//        jobQueueUpdate();
        worker.pushHead(job);
    }

    private synchronized void pushJob(Runnable job) {
//        jobQueue.add(job);
//        jobQueueUpdate();
        worker.push(job);
    }

//    private synchronized void jobQueueUpdate() {
//        if (workingThread == null || workingThread.isInterrupted()) {
//            workingThread = new Thread(workingRunnable);
//            workingThread.start();
//        }
//    }

    private void onRequestError(Exception e) {
        e.printStackTrace();
        synchronized (this) {
            worker.clear();
            mapData_.clear();
            sweepMapData_.clear();
            robotPlatform_ = null;
            connected_ = false;
        }

        EventBus.getDefault().post(new ConnectionLostEvent());
    }

    private class JobConnect implements Runnable {
        public JobConnect() {
        }

        @Override
        public void run() {
            try {
                String ip;
                int port;
                synchronized (RPSlamwareSdpAgent.this) {
                    ip = ip_;
                    port = port_;
                }

                if (ip == null || ip.isEmpty() || port <= 0|| port > 65535) {
                    onRequestError(new Exception());
                    return;
                }

                AbstractSlamwarePlatform robotPlatform = DeviceManager.connect(ip, port);

                Pose robotPose = robotPlatform.getPose();
                List<MapType> mapTypes = robotPlatform.getAvailableMaps();
                MapType mapType = mapTypes.get(0);

                RectF knownArea = robotPlatform.getKnownArea(mapType);
                LaserScan currentLaserScan = robotPlatform.getLaserScan();
                String sdpVersion = robotPlatform.getSlamwareVersion();

                shouldReadLocalizationQuliaty = false;
                String[] versions = sdpVersion.split("[.]");
                if (versions.length >= 2) {
                    if (Integer.valueOf(versions[0]) > 1) {
                        shouldReadLocalizationQuliaty = true;
                    } else if (Integer.valueOf(versions[0]) == 1 && Integer.valueOf(versions[1]) > 7) {
                        shouldReadLocalizationQuliaty = true;
                    } else if (versions.length >= 3 && Integer.valueOf(versions[0]) == 1 &&
                            Integer.valueOf(versions[1]) == 7 && Integer.valueOf(versions[2]) >= 1) {
                        shouldReadLocalizationQuliaty = true;
                    }
                }

                synchronized (RPSlamwareSdpAgent.this) {
                    robotPose_ = robotPose;
                    robotPlatform_ = robotPlatform;
                    mapType_ = mapType;

                    laserScan_ = currentLaserScan;
                    sdpVersion_ = sdpVersion;

                    connected_ = true;
                }

                RectF clipArea = new RectF(
                        (float)(robotPose.getLocation().getX() - SLAMWARE_MAX_UPDATE_MAP_RADIX * 2),
                        (float)(robotPose.getLocation().getY() - SLAMWARE_MAX_UPDATE_MAP_RADIX * 2),
                        (float)(robotPose.getLocation().getX() + SLAMWARE_MAX_UPDATE_MAP_RADIX * 2),
                        (float)(robotPose.getLocation().getY() + SLAMWARE_MAX_UPDATE_MAP_RADIX * 2)
                );

                knownArea.intersect(clipArea);

                updateMap(knownArea);
            } catch (Exception exception) {
                onRequestError(exception);
                return;
            }

            EventBus.getDefault().post(new ConnectedEvent());
        }
    }

    private class JobDisconnect implements Runnable {
        @Override
        public void run() {
            synchronized (RPSlamwareSdpAgent.this) {
                if (robotPlatform_ == null) {
                    return;
                }
                worker.clear();
                mapData_.clear();
                sweepMapData_.clear();
//                jobQueue.clear();
                robotPlatform_.disconnect();
                robotPlatform_ = null;
                connected_ = false;
            }
        }
    }

    private class JobUpdateMap implements Runnable {
        @Override
        public void run() {
            AbstractSlamwarePlatform platform;
            RectF area = new RectF();
            MapType mapType;

            synchronized (RPSlamwareSdpAgent.this) {
                RectF tmp = new RectF(mergedMapUpdateArea_);
                mergedMapUpdateArea_.set(area);
                area.set(tmp);
                platform = robotPlatform_;
                mapType = mapType_;
            }

            if (platform == null) {
                return;
            }

            if (area.isEmpty()) {
                EventBus.getDefault().post(new MapUpdateEvent(area));
                return;
            }

            Map map;
            Map sweepMap;

            try {
                long time = System.currentTimeMillis();
                map = platform.getMap(mapType, MapKind.EXPLORE_MAP, area);
                sweepMap = platform.getMap(mapType, MapKind.SWEEP_MAP, area);
            } catch (Exception e) {
                onRequestError(e);
                return;
            }

            synchronized (RPSlamwareSdpAgent.this) {
                long t = System.currentTimeMillis();
                mapData_.update(map);
                sweepMapData_.update(sweepMap);
            }

            EventBus.getDefault().post(new MapUpdateEvent(area));
        }
    }

    private class JobUpdateWholeMap implements Runnable {
        @Override
        public void run() {
            AbstractSlamwarePlatform platform;
            MapType mapType;
            Pose robotPose;

            synchronized (RPSlamwareSdpAgent.this) {
                platform = robotPlatform_;
                mapType = mapType_;
                robotPose = robotPose_;
            }

            if (platform == null) {
                return;
            }

            try {
                RectF area = platform.getKnownArea(mapType);

//                RectF clipArea = new RectF(
//                        (robotPose.getLocation().getX() - SLAMWARE_MAX_UPDATE_MAP_RADIX),
//                        (robotPose.getLocation().getY() - SLAMWARE_MAX_UPDATE_MAP_RADIX),
//                        (robotPose.getLocation().getX() + SLAMWARE_MAX_UPDATE_MAP_RADIX),
//                        (robotPose.getLocation().getY() + SLAMWARE_MAX_UPDATE_MAP_RADIX)
//                );
//
//                area.intersect(clipArea);

                updateMap(area);
            } catch (Exception e) {
                onRequestError(e);
            }
        }
    }

    private class JobUpdatePose implements Runnable {
        @Override
        public void run() {
            AbstractSlamwarePlatform platform;
            synchronized (RPSlamwareSdpAgent.this) {
                platform = robotPlatform_;
                if (platform == null) {
                    updatingPose_ = false;
                    return;
                }
            }

            Pose pose;

            try {
                pose = platform.getPose();
                synchronized (RPSlamwareSdpAgent.this) {
                    robotPose_ = pose;
                    updatingPose_ = false;
                }
            } catch (Exception e) {
                onRequestError(e);
                updatingPose_ = false;
                return;
            }

            EventBus.getDefault().post(new RobotPoseUpdateEvent(pose));
        }
    }

    private class JobUpdateLaserScan implements Runnable {
        @Override
        public void run() {
            AbstractSlamwarePlatform platform;
            synchronized (RPSlamwareSdpAgent.this) {
                platform = robotPlatform_;
                if (platform == null) {
                    updatingLaserScan_ = false;
                    return;
                }
            }

            LaserScan laserScan;
            Pose pose = null;

            try {
                laserScan = platform.getLaserScan();
                if (laserScan.getPose()!=null) {
                    pose = laserScan.getPose();
                }
            } catch (Exception e) {
                onRequestError(e);
                updatingLaserScan_ = false;
                return;
            }

            synchronized (RPSlamwareSdpAgent.this) {
                laserScan_ = laserScan;
                if (laserScan.getPose() != null && pose != null) {
                    robotPose_ = pose;
                }
                updatingLaserScan_ = false;
            }

            EventBus.getDefault().post(new LaserScanUpdateEvent(laserScan));
        }
    }

    private class JobUpdateStatus implements Runnable {
        @Override
        public void run() {
            AbstractSlamwarePlatform platform;
            synchronized (RPSlamwareSdpAgent.this) {
                platform = robotPlatform_;
                if (platform == null) {
                    updatingStatus_ = false;
                    return;
                }
            }

            int batteryPercentage;
            boolean isCharging;
            int localizationQuality;

            try {
                batteryPercentage = platform.getBatteryPercentage();
                isCharging = platform.getBatteryIsCharging();
                localizationQuality = shouldReadLocalizationQuliaty ? platform.getLocalizationQuality() : -1;
            } catch (Exception e) {
                onRequestError(e);
                updatingStatus_ = false;
                return;
            }

            synchronized (RPSlamwareSdpAgent.this) {
                updatingStatus_ = false;
            }

            EventBus.getDefault().post(new RobotStatusUpdateEvent(batteryPercentage, isCharging, localizationQuality));
        }
    }

    private class JobUpdateMoveAction implements Runnable {
        @Override
        public void run() {
            AbstractSlamwarePlatform platform;
            synchronized (RPSlamwareSdpAgent.this) {
                platform = robotPlatform_;
                if (platform == null) {
                    updatingMoveAction_ = false;
                    return;
                }
            }

            IMoveAction moveAction;
            Path remainingMilestones = null;
            Path remainingPath = null;

            try {
                moveAction = platform.getCurrentAction();
                if (moveAction != null) {
                    if (OperateAction.shouldStop(moveAction.getActionName())) {
                        moveAction.cancel();
                        OperateAction.reset();
                    }
                    remainingMilestones = moveAction.getRemainingMilestones();
                    remainingPath = moveAction.getRemainingPath();
                }
            } catch (Exception e) {
                onRequestError(e);
                updatingMoveAction_ = false;
                return;
            }

            synchronized (RPSlamwareSdpAgent.this) {
                moveAction_ = moveAction;
                updatingMoveAction_ = false;
            }

            EventBus.getDefault().post(new MoveActionUpdateEvent(remainingMilestones, remainingPath));
        }
    }

    private class JobMoveTo implements Runnable {
        public JobMoveTo() {
        }

        @Override
        public void run() {
            AbstractSlamwarePlatform platform;
            Location location;
            synchronized (RPSlamwareSdpAgent.this) {
                platform = robotPlatform_;
                location = location_;
                if (platform == null || location == null)
                    return;
            }

            try {
                platform.moveTo(location);
            } catch (Exception e) {
                onRequestError(e);
            }
        }
    }

    private class JobSweepSpot implements Runnable {
        @Override
        public void run() {
            AbstractSlamwarePlatform platform;
            Location location;
            synchronized (RPSlamwareSdpAgent.this) {
                platform = robotPlatform_;
                location = location_;
                if (platform == null || location == null) {
                    return;
                }
            }

            try {
                platform.sweepSpot(location);
            } catch (Exception e) {
                onRequestError(e);
            }
        }
    }

    private class JobRotateTo implements Runnable {
        @Override
        public void run() {
            AbstractSlamwarePlatform platform;
            Rotation targetRotation;

            synchronized (RPSlamwareSdpAgent.this) {
                platform = robotPlatform_;
                if (platform == null) {
                    rotating_ = false;
                    return;
                }
                targetRotation = targetRotation_;
            }

            try {
                platform.rotateTo(targetRotation);
            } catch (Exception e) {
                onRequestError(e);
            }
            rotating_ = false;
        }
    }

    private class JobMoveBy implements Runnable {
        public JobMoveBy() {
        }

        @Override
        public void run() {
            AbstractSlamwarePlatform platform;
            MoveDirection direction;
            synchronized (RPSlamwareSdpAgent.this) {
                platform = robotPlatform_;
                direction = direction_;
                if (platform == null || direction == null) {
                    return;
                }
            }
            try {
                platform.moveBy(direction);
            } catch (Exception e) {
                onRequestError(e);
            }

            synchronized (RPSlamwareSdpAgent.this) {
                direction_ = null;
            }
        }
    }

    private class JobClearMap implements Runnable {
        @Override
        public void run() {
            AbstractSlamwarePlatform platform;
            RectF area;
            MapType mapType;

            synchronized (RPSlamwareSdpAgent.this) {
                platform = robotPlatform_;
                mapType = mapType_;
            }

            if (platform == null)
                return;

            try {
//                area = platform.getKnownArea(mapType);
                platform.clearMap();
            } catch (Exception e) {
                onRequestError(e);
                return;
            }

            synchronized (RPSlamwareSdpAgent.this) {
                mapData_.clear();
                sweepMapData_.clear();
            }

            EventBus.getDefault().post(new MapUpdateEvent(new RectF(-60, -60, 60, 60)));
        }
    }

    private class JobUpdateWalls implements Runnable {
        @Override
        public void run() {
            AbstractSlamwarePlatform platform;
            synchronized (RPSlamwareSdpAgent.this) {
                platform = robotPlatform_;
                if (platform == null) {
                    updatingWalls_ = false;
                    return;
                }
            }

            Vector<Line> walls;

            try {
                walls = platform.getWalls();
            } catch (Exception e) {
                onRequestError(e);
                updatingWalls_ = false;
                return;
            }

            synchronized (RPSlamwareSdpAgent.this) {
                walls_ = walls;
            }

            updatingWalls_ = false;

            EventBus.getDefault().post(new WallUpdateEvent(walls));
        }
    }

    private class JobAddWalls implements Runnable {
        @Override
        public void run() {
            AbstractSlamwarePlatform platform;
            Vector<Line> wallsToAdd = new Vector<>();

            synchronized (RPSlamwareSdpAgent.this) {
                platform = robotPlatform_;
                if (platform == null)
                    return;
                Vector<Line> tmp = new Vector<>(wallsToAdd_);
                wallsToAdd_.clear();
                wallsToAdd.clear();
                wallsToAdd.addAll(tmp);
            }

            if (wallsToAdd.isEmpty())
                return;

            try {
                platform.addWalls(wallsToAdd);
            } catch (Exception e) {
                onRequestError(e);
            }

            updateWalls();
        }
    }

    private class JobClearWalls implements Runnable {
        @Override
        public void run() {
            AbstractSlamwarePlatform platform;
            synchronized (RPSlamwareSdpAgent.this) {
                platform = robotPlatform_;
                if (platform == null)
                    return;
            }
            try {
                platform.clearWalls();
            } catch (Exception e) {
                onRequestError(e);
            }

            updateWalls();
        }
    }

    private class JobRemoveWallById implements Runnable {
        @Override
        public void run() {
            AbstractSlamwarePlatform platform;
            int wallId;

            synchronized (RPSlamwareSdpAgent.this) {
                platform = robotPlatform_;
                if (platform == null) {
                    return;
                }
                wallId = wallId_;
            }

            try {
                platform.clearWallById(wallId);
            } catch (Exception e) {
                onRequestError(e);
            }

            updateWalls();
        }
    }

    private class JobStartSweep implements Runnable {
        @Override
        public void run() {
            AbstractSlamwarePlatform platform;
            synchronized (RPSlamwareSdpAgent.this) {
                platform = robotPlatform_;
            }

            if (platform == null)
                return;

            try {
                platform.startSweep();
            } catch (Exception e) {
                onRequestError(e);
            }
        }
    }

    private class JobGoHome implements Runnable {
        @Override
        public void run() {
            AbstractSlamwarePlatform platform;
            synchronized (RPSlamwareSdpAgent.this) {
                platform = robotPlatform_;
            }
            if (platform == null)
                return;

            try {
                platform.goHome();
            } catch (Exception e) {
                onRequestError(e);
            }
        }
    }

    private class JobCancelAllActions implements Runnable {
        @Override
        public void run() {
            AbstractSlamwarePlatform platform;
            IMoveAction moveAction;
            synchronized (RPSlamwareSdpAgent.this) {
                platform = robotPlatform_;
            }
            if (platform == null)
                return;

            try {
                moveAction = platform.getCurrentAction();
                if (moveAction != null) {
                    moveAction.cancel();
                }
            } catch (Exception e) {
                onRequestError(e);
            }

            worker.clear();

            synchronized (RPSlamwareSdpAgent.this) {
//                jobQueue.clear();
                updatingStatus_ = false;
                updatingMoveAction_ = false;
                updatingLaserScan_ = false;
                updatingPose_ = false;
                updatingWalls_ = false;
                rotating_ = false;
            }

            EventBus.getDefault().post(new MapUpdateEvent(new RectF()));
        }
    }

    private class JobGetRobotHealth implements Runnable {
        @Override
        public void run() {
            AbstractSlamwarePlatform platform;
            synchronized (RPSlamwareSdpAgent.this) {
                platform = robotPlatform_;
            }

            if (platform == null) {
                return;
            }

            try {
//                HealthInfo info = platform.getRobotHealth();
//                EventBus.getDefault().post(new RobotHealthInfoEvent(info));
            } catch (Exception e) {
                onRequestError(e);
            }
        }
    }
}
