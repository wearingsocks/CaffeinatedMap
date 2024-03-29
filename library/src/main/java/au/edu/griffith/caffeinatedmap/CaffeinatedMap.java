package au.edu.griffith.caffeinatedmap;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;

import java.lang.ref.WeakReference;
import java.util.List;

import au.edu.griffith.caffeinatedmap.clustering.ClusterHandler;
import au.edu.griffith.caffeinatedmap.clustering.ClusteringSettings;
import au.edu.griffith.caffeinatedmap.markers.CaffeinatedMarkerOptions;

public class CaffeinatedMap extends BaseCaffeinatedMap {

    private GoogleMap.OnCameraChangeListener mCameraChangeListener;
    private ClusterHandler mClusterHandler;

    protected CaffeinatedMap(WeakReference<GoogleMap> mapReference) {
        super(mapReference);
        mClusterHandler = new ClusterHandler(mMapReference);
        super.setOnCameraChangeListener(new OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                onCaffeinatedCameraChange(cameraPosition);
            }
        });
    }

    public void addClusterableMarker(CaffeinatedMarkerOptions options) {
        if (mClusterHandler != null) {
            mClusterHandler.addMarker(options);
        }
    }

    public void addClusterableMarkers(List<CaffeinatedMarkerOptions> list) {
        if (mClusterHandler != null) {
            mClusterHandler.addMarkers(list);
        }
    }

    public void clearClusterableMarkers() {
        if (mClusterHandler != null) {
            mClusterHandler.clearMarkers();
        }
    }

    public void forceUpdate() {
        if (mClusterHandler != null) {
            mClusterHandler.forceUpdate();
        }
    }

    public ClusteringSettings getClusteringSettings() {
        return (mClusterHandler != null) ? mClusterHandler.getSettings() : null;
    }

    public Marker getMarker(String key) {
        return (mClusterHandler != null) ? mClusterHandler.getMarker(key) : null;
    }

    @Override
    public void setOnCameraChangeListener(GoogleMap.OnCameraChangeListener listener) {
        mCameraChangeListener = listener;
    }

    private void onCaffeinatedCameraChange(CameraPosition cameraPosition) {
        if (mClusterHandler != null) {
            mClusterHandler.updateClusters();
        }
        if (mCameraChangeListener != null) {
            mCameraChangeListener.onCameraChange(cameraPosition);
        }
    }

}
