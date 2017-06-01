package umd.mindlab.main;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.esri.arcgisruntime.layers.ArcGISMapImageLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalInfo;
import com.esri.arcgisruntime.portal.PortalItem;
import com.esri.arcgisruntime.portal.PortalUser;
import com.esri.arcgisruntime.security.AuthenticationManager;
import com.esri.arcgisruntime.security.DefaultAuthenticationChallengeHandler;
import com.esri.arcgisruntime.security.UserCredential;


public class map extends Activity {

    private MapView mMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mMapView = (MapView) findViewById(R.id.mapView);

//        ArcGISMap map = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, 38.99029, -76.9361, 16);
//        mMapView.setMap(map);

//        // Set the DefaultAuthenticationChallegeHandler to allow authentication with the portal.
//        DefaultAuthenticationChallengeHandler handler = new DefaultAuthenticationChallengeHandler(this);
//        AuthenticationManager.setAuthenticationChallengeHandler(handler);

        // create a new Portal object
        final Portal portal = new Portal("http://uofmd.maps.arcgis.com",true);
        // create a Map which is loaded from a webmap
        UserCredential creds = new UserCredential("maracai77", "locus4160");
        portal.setCredential(creds);
        portal.addDoneLoadingListener(new Runnable(){
            @Override
            public void run() {
                if (portal.getLoadStatus() == LoadStatus.LOADED) {
                    PortalUser user = portal.getUser();
                    Log.v("Map","user: "+user.getFullName());

                }
            }
        });
        portal.loadAsync();

        // create a PortalItem based on a pre-defined portal id
        final PortalItem portalItem = new PortalItem(portal, "41490d54d2044c1eaf20ae7776ebc85c");
        portalItem.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                Log.v("Map","portalitem loaded done");
                Log.v("Mapp","here: "+portalItem.getOwner());
            }
        });
        portalItem.loadAsync();

        final ArcGISMapImageLayer layer = new ArcGISMapImageLayer("https://gis.fm.umd.edu/arcgis/rest/services/InteriorSpace/GISFloorplansALL/MapServer");
        UserCredential creds2 = new UserCredential("agrawala", "aa1234");
        layer.setCredential(creds2);
        layer.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                Log.v("Map","layer: "+layer.getName());
            }
        });
        layer.loadAsync();

        // create a map from a PortalItem

        ArcGISMap map = new ArcGISMap(portalItem);
        //load the map
        map.loadAsync();

        //listen to when it is loaded
        map.addDoneLoadingListener(new Runnable() {

            @Override
            public void run() {
                System.out.println("map loaded");
            }
        });
        // set the map to be displayed in a MapView
        mMapView.setMap(map);

    }

    @Override
    protected void onPause(){
        mMapView.pause();
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mMapView.resume();
    }
}
