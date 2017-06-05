package umd.mindlab.main;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.internal.jni.CoreRequest;
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalInfo;
import com.esri.arcgisruntime.portal.PortalItem;
import com.esri.arcgisruntime.portal.PortalUser;
import com.esri.arcgisruntime.portal.PortalUserContent;
import com.esri.arcgisruntime.security.AuthenticationManager;
import com.esri.arcgisruntime.security.DefaultAuthenticationChallengeHandler;
import com.esri.arcgisruntime.security.UserCredential;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters;
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult;
import com.esri.arcgisruntime.tasks.geocode.LocatorAttribute;
import com.esri.arcgisruntime.tasks.geocode.LocatorInfo;
import com.esri.arcgisruntime.tasks.geocode.LocatorTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class map extends Activity {

    Context context;
    private MapView mMapView;
    EditText location;
    Button search,gpsbutton;
    GraphicsOverlay graphicsOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        context = this;
        mMapView = (MapView) findViewById(R.id.mapView);
        location = (EditText) findViewById(R.id.location);
        search = (Button) findViewById(R.id.search);
        gpsbutton = (Button) findViewById(R.id.gpsbutton);

//        ArcGISMap map = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, 38.99029, -76.9361, 16);
//        mMapView.setMap(map);

        // Set the DefaultAuthenticationChallegeHandler to allow authentication with the portal.
        DefaultAuthenticationChallengeHandler handler = new DefaultAuthenticationChallengeHandler(this);
        AuthenticationManager.setAuthenticationChallengeHandler(handler);

        // create a new Portal object
        final Portal portal = new Portal("http://uofmd.maps.arcgis.com/",true);
        // create a Map which is loaded from a webmap
        UserCredential creds = new UserCredential("maracai77", "locus4160");
        portal.setCredential(creds);
        portal.addDoneLoadingListener(new Runnable(){
            @Override
            public void run() {
                if (portal.getLoadStatus() == LoadStatus.LOADED) {
                    PortalUser user = portal.getUser();
                    Log.v("Map","user: "+user.getFullName());
                    Log.v("Map","portal loaded done");
                    Log.v("Map","portal status: "+portal.getLoadStatus());
                }
            }
        });
//        portal.loadAsync();


        // create a PortalItem based on a pre-defined portal id
        final PortalItem portalItem = new PortalItem(portal, "30010341c0d3439ab3337cf79d70dc6b");
        portalItem.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                Log.v("Mapp","here: "+portalItem.getOwner());
                Log.v("Map","portalitem loaded done");
                Log.v("Map","portalitem status: "+portalItem.getLoadStatus());
                Log.v("Map","portal item type: "+portalItem.getType());
            }
        });
//        portalItem.loadAsync();
        final ArcGISMapImageLayer mapImageLayer = new ArcGISMapImageLayer("https://gis.fm.umd.edu/arcgis/rest/services/InteriorSpace/GISFloorplansALL/MapServer");
        UserCredential creds2 = new UserCredential("agrawala", "aa1234");
        mapImageLayer.setCredential(creds2);
        mapImageLayer.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                Log.v("Map","layer: "+mapImageLayer.getName());
                Log.v("Map","layer loaded done");
                Log.v("Map","layer status: "+mapImageLayer.getLoadStatus());
            }
        });
//        layer.loadAsync();




        // create a map from a PortalItem
//        while(!portalItem.getLoadStatus().equals("LOADED"));
        Log.v("map","here portal id: "+portalItem.getItemId());
        final ArcGISMap map = new ArcGISMap(portalItem);
        Log.v("Map","map status1: "+map.getLoadStatus());
        //load the map
//        map.loadAsync();
        Log.v("Map","map status2: "+map.getLoadStatus());

        //listen to when it is loaded
        map.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                System.out.println("map loaded done");
                Log.v("Map","map status: "+map.getLoadStatus());
//                map.retryLoadAsync();
            }
        });
        // add map image layer as operational layer
        map.getOperationalLayers().add(mapImageLayer);

        // set the map to be displayed in a MapView
        mMapView.setMap(map);

        Log.v("Map","map status3: "+map.getLoadStatus());

        // Create a LocatorTask using an online locator
        final LocatorTask locatorTask = new LocatorTask("http://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer");

        final GeocodeParameters mGeocodeParameters = new GeocodeParameters();
        mGeocodeParameters.getResultAttributeNames().add("*");
        mGeocodeParameters.setMaxResults(6);
        final GeocodeResult[] mGeocodedLocation = {null};

        locatorTask.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                if (locatorTask.getLoadStatus() == LoadStatus.LOADED) {
                    // Locator is ready to use
                    Log.v("Map","locatorTask loaded");

                } else {
                    Log.i("Map", "Trying to reload locator task");
                    locatorTask.retryLoadAsync();
                }
            }
        });

        locatorTask.loadAsync();
//        final GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
//        final ListenableFuture<List<GeocodeResult>> geocodeFuture = onlineLocator.geocodeAsync("380 New York Street, Redlands, CA");
//        geocodeFuture.addDoneListener(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    // Get the results of the async operation
//                    List<GeocodeResult> geocodeResults = geocodeFuture.get();
//
//                    if (geocodeResults.size() > 0) {
//                        // Use the first result - for example display in an existing Graphics Overlay
//                        GeocodeResult topResult = geocodeResults.get(0);
//                        Log.v("Map","location: "+topResult.getDisplayLocation());
//                        Graphic gecodedLocation = new Graphic(topResult.getDisplayLocation(), topResult.getAttributes(),
//                                new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.SQUARE, Color.rgb(255, 0, 0), 20.0f));
//                        // add graphic to overlay
//                        graphicsOverlay.getGraphics().add(gecodedLocation);
//                        // add graphics overlay to the MapView
//                        mMapView.getGraphicsOverlays().add(graphicsOverlay);
//                        // create the London location point
//                        Point londonPoint = new Point(topResult.getDisplayLocation().getX(),topResult.getDisplayLocation().getY(), SpatialReference.create(2229));
//                        // create the viewpoint with the London point and scale
//                        Viewpoint viewpoint = new Viewpoint(londonPoint,7000);
//                        // set the map views's viewpoint to London with a ten second duration
//                        mMapView.setViewpoint(viewpoint);
//                    }
//                }
//                catch (InterruptedException | ExecutionException e) {
//                    e.printStackTrace();
//                }
//            }
//        });


        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ListenableFuture<List<GeocodeResult>> geocodeFuture = locatorTask.geocodeAsync(location.getText().toString(),
                        mGeocodeParameters);
                geocodeFuture.addDoneListener(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Get the results of the async operation
                            List<GeocodeResult> geocodeResults = geocodeFuture.get();

                            if (geocodeResults.size() > 0) {
                                // Use the first result - for example
                                // display on the map
                                mGeocodedLocation[0] = geocodeResults.get(0);
                                displaySearchResult(mGeocodedLocation[0].getDisplayLocation(), mGeocodedLocation[0].getLabel());

                                for(int i=0;i<geocodeResults.size();i++){
                                    Log.v("Map","search: "+geocodeResults.get(i).getDisplayLocation()+" , "+geocodeResults.get(i).getLabel());
                                }

//                                Point point = new Point(mGeocodedLocation[0].getDisplayLocation().getX(),mGeocodedLocation[0].getDisplayLocation().getY(), SpatialReference.create(2229));
//                                Viewpoint viewpoint = new Viewpoint(point,7000);
//                                mMapView.setViewpoint(viewpoint);

                            } else {
                                Toast.makeText(getApplicationContext(), "not found", Toast.LENGTH_LONG).show();
                            }

                        } catch (InterruptedException | ExecutionException e) {
                            // Deal with exception...
                            e.printStackTrace();
                        }
                        // Done processing and can remove this listener.
                        geocodeFuture.removeDoneListener(this);
                    }
                });


//                Polygon polygon = mMapView.getVisibleArea();
//                Toast.makeText(context,polygon.toJson(),Toast.LENGTH_LONG).show();

//                Geometry gm =(Geometry) Geometry.fromJson("{\n" +
//                        "    \"rings\": [\n" +
//                        "     [\n" +
//                        "      [\n" +
//                        "       -8564514.0826235712,\n" +
//                        "       4720294.659030511\n" +
//                        "      ],\n" +
//                        "      [\n" +
//                        "       -8564510.6907497682,\n" +
//                        "       4720294.6806521676\n" +
//                        "      ],\n" +
//                        "      [\n" +
//                        "       -8564510.646406075,\n" +
//                        "       4720287.6727929674\n" +
//                        "      ],\n" +
//                        "      [\n" +
//                        "       -8564510.9072501417,\n" +
//                        "       4720287.6710404372\n" +
//                        "      ],\n" +
//                        "      [\n" +
//                        "       -8564510.9014624748,\n" +
//                        "       4720286.7542403964\n" +
//                        "      ],\n" +
//                        "      [\n" +
//                        "       -8564510.6406185217,\n" +
//                        "       4720286.7558639077\n" +
//                        "      ],\n" +
//                        "      [\n" +
//                        "       -8564510.6238300242,\n" +
//                        "       4720284.1033886811\n" +
//                        "      ],\n" +
//                        "      [\n" +
//                        "       -8564519.6905330699,\n" +
//                        "       4720284.0454856968\n" +
//                        "      ],\n" +
//                        "      [\n" +
//                        "       -8564519.6921502259,\n" +
//                        "       4720284.3073915951\n" +
//                        "      ],\n" +
//                        "      [\n" +
//                        "       -8564520.2792417575,\n" +
//                        "       4720284.303673787\n" +
//                        "      ],\n" +
//                        "      [\n" +
//                        "       -8564520.3642395549,\n" +
//                        "       4720297.7300386578\n" +
//                        "      ],\n" +
//                        "      [\n" +
//                        "       -8564514.1023068782,\n" +
//                        "       4720297.7700379519\n" +
//                        "      ],\n" +
//                        "      [\n" +
//                        "       -8564514.0826235712,\n" +
//                        "       4720294.659030511\n" +
//                        "      ]\n" +
//                        "     ]\n" +
//                        "    ],\n" +
//                        "    \"spatialReference\": {\n" +
//                        "     \"wkid\": 102100,\n" +
//                        "     \"latestWkid\": 3857\n" +
//                        "    }\n" +
//                        "   }");
//                mMapView.setViewpointGeometryAsync(gm);
            }
        });


        final LocationDisplay mLocationDisplay;
        // get the MapView's LocationDisplay
        mLocationDisplay = mMapView.getLocationDisplay();

        // Listen to changes in the status of the location data source.
        mLocationDisplay.addDataSourceStatusChangedListener(new LocationDisplay.DataSourceStatusChangedListener() {
            @Override
            public void onStatusChanged(LocationDisplay.DataSourceStatusChangedEvent dataSourceStatusChangedEvent) {

                // If LocationDisplay started OK, then continue.
                if (dataSourceStatusChangedEvent.isStarted())
                    return;

                // No error is reported, then continue.
                if (dataSourceStatusChangedEvent.getError() == null)
                    return;

            }
        });
        gpsbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLocationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.RECENTER);
                if (!mLocationDisplay.isStarted())
                    mLocationDisplay.startAsync();
            }
        });


    }

    void displaySearchResult(Point resultPoint, String address) {

        if (mMapView.getCallout().isShowing()) {
            mMapView.getCallout().dismiss();
        }

        // Zoom map to geocode result location
        mMapView.setViewpointAsync(new Viewpoint(resultPoint, 8000), 2);
    }

    @Override
    protected void onPause(){
        super.onPause();
        mMapView.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mMapView.resume();
    }
}
