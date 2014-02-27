package au.edu.griffith.caffeinatedmap.clustering;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import au.edu.griffith.caffeinatedmap.markers.CaffeinatedMarkerOptions;

public class ClusterHandler {

    private final WeakReference<GoogleMap> mMapReference;
    private ClusteringSettings mSettings;

    private HashMap<String, CaffeinatedMarkerOptions> mCaffeinatedMarkerOptions;
    private HashMap<String, Marker> mVisibleClusters;
    private List<Clusterable> mClusterables;
    private List<Cluster> mCurrentClusters;
    private float mZoomLevel;

    public ClusterHandler(WeakReference<GoogleMap> mapReference) {
        mMapReference = mapReference;
        mSettings = new ClusteringSettings().setClusterOptions(new ClusterOptions());

        mCaffeinatedMarkerOptions = new HashMap<String, CaffeinatedMarkerOptions>();
        mClusterables = new ArrayList<Clusterable>();
        mVisibleClusters = new HashMap<String, Marker>();
        mZoomLevel = 0f;
    }

    public ClusteringSettings getSettings() {
        return mSettings;
    }

    public void addMarker(CaffeinatedMarkerOptions options) {
        if (options != null) {
            mCaffeinatedMarkerOptions.put(options.getKey(), options);
            Clusterable clusterable = new Clusterable(options.getKey(), options.getType());
            clusterable.setPosition(options.getPosition());
            mClusterables.add(clusterable);
        }
    }

    public void addMarkers(List<CaffeinatedMarkerOptions> list) {
        if (list != null) {
            for (CaffeinatedMarkerOptions options : list) {
                addMarker(options);
            }
        }
    }

    public void clearMarkers() {
        removeAllClustersFromMap();
        mClusterables = new ArrayList<Clusterable>();
        mCaffeinatedMarkerOptions = new HashMap<String, CaffeinatedMarkerOptions>();
    }

    public List<Cluster> getClusters() {
        return mCurrentClusters;
    }

    public void updateClusters() {
        GoogleMap googleMap = mMapReference.get();
        if (googleMap != null) {
            float zoom = googleMap.getCameraPosition().zoom;
            if ((int) mZoomLevel != (int) zoom) {
                mZoomLevel = zoom;
                ClusterBuildTask.BuildTaskArgs buildTaskArgs = new ClusterBuildTask.BuildTaskArgs();
                buildTaskArgs.settings = mSettings;
                buildTaskArgs.projection = googleMap.getProjection();
                buildTaskArgs.clusterables = mClusterables;
                new ClusterBuildTask(new ClusterBuildTask.BuildTaskCallback() {
                    @Override
                    public void onBuildTaskReturn(List<Cluster> clusters) {
                        onClusterBuildTaskReturn(clusters);
                    }
                }).execute(buildTaskArgs);
            } else {
                updateVisibleClusters();
            }
        }
    }

    public void forceUpdate() {
        mZoomLevel = 0f;
        updateClusters();
    }

    private void updateVisibleClusters() {
        GoogleMap googleMap = mMapReference.get();
        if (googleMap != null && mCurrentClusters != null) {
            LatLngBounds visibleBounds = googleMap.getProjection().getVisibleRegion().latLngBounds;

            for (Cluster cluster : mCurrentClusters) {
                if (visibleBounds.contains(cluster.getPosition())) {
                    if (cluster.size() > 1) {
                        addClusterToMap(cluster.getKey(), getClusterMakerOptions(cluster));
                    } else if (cluster.size() == 1) {
                        addClusterToMap(cluster.getKey(), getCMO(cluster.getClusterableAtIndex(0).getCMOKey()));
                    }
                } else {
                    removeClusterFromMap(cluster.getKey());
                }
            }
        }
    }

    private MarkerOptions getCMO(String key) {
        // TODO Polygons
        if (mCaffeinatedMarkerOptions.containsKey(key)) {
            return mCaffeinatedMarkerOptions.get(key).getMarkerOptions();
        }
        return null;
    }

    private MarkerOptions getClusterMakerOptions(Cluster cluster) {
        MarkerOptions options = new MarkerOptions();
        // TODO Check For Custom Options
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        options.title(cluster.getKey());

        options.position(cluster.getPosition());
        return options;
    }

    private void addClusterToMap(String key, MarkerOptions options) {
        if (!mVisibleClusters.containsKey(key)) {
            GoogleMap googleMap = mMapReference.get();
            if (googleMap != null && options != null) {
                mVisibleClusters.put(key, googleMap.addMarker(options));
            }
        }
    }

    private void removeClusterFromMap(String key) {
        if (mVisibleClusters.containsKey(key)) {
            mVisibleClusters.get(key).remove();
            mVisibleClusters.remove(key);
        }
    }

    private void removeAllClustersFromMap() {
        for (Marker marker : mVisibleClusters.values()) {
            marker.remove();
        }
        mVisibleClusters = new HashMap<String, Marker>();
    }

    private void onClusterBuildTaskReturn(List<Cluster> clusters) {
        mCurrentClusters = clusters;
        removeAllClustersFromMap();
        updateVisibleClusters();
    }

}
