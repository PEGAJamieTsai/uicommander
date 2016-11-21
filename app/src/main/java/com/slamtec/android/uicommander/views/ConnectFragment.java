package com.slamtec.android.uicommander.views;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.slamtec.android.uicommander.R;
import com.slamtec.android.uicommander.utils.LogUtil;
import com.slamtec.android.uicommander.views.controls.RPDeviceCell;
import com.slamtec.slamware.discovery.AbstractDiscover;
import com.slamtec.slamware.discovery.BleDevice;
import com.slamtec.slamware.discovery.Device;
import com.slamtec.slamware.discovery.DeviceManager;
import com.slamtec.slamware.discovery.DiscoveryMode;
import com.slamtec.slamware.discovery.MdnsDevice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConnectFragment extends Fragment {
    private final static String TAG = ConnectFragment.class.getName();

    private ArrayList<Device> devices = new ArrayList<>();

    private IConnectFragmentInteractionListener mListener;

    private SwipeRefreshLayout swipeRefreshLayout;

    private boolean scanning;

    private DeviceManager deviceManager;

    private AbstractDiscover.DiscoveryListener discoveryListener = new AbstractDiscover.DiscoveryListener() {
        @Override
        public void onStartDiscovery(AbstractDiscover discover) {
            LogUtil.d(TAG, discover.getMode().toString() + " start discover");
        }

        @Override
        public void onStopDiscovery(AbstractDiscover discover) {
            LogUtil.d(TAG, discover.getMode().toString() + " stop discover");
        }

        @Override
        public void onDiscoveryError(AbstractDiscover discover, String error) {
            LogUtil.d(TAG, discover.getMode().toString() + " discover error: " + error);

            if (discover.getMode() == DiscoveryMode.BLE) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "Please open bluetooth", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        @Override
        public void onDeviceFound(AbstractDiscover discover, Device device) {
            for (Device d : devices) {
                if (d.canBeFoundWith(DiscoveryMode.BLE) == device.canBeFoundWith(DiscoveryMode.BLE)
                        && d.getDeviceName().equals(device.getDeviceName())) {
                    return;
                }
            }

            devices.add(device);

            Collections.sort(devices, new Comparator<Device>() {
                @Override
                public int compare(Device lhs, Device rhs) {
                    if (lhs.canBeFoundWith(DiscoveryMode.BLE)) {
                        return -1;
                    } else if (rhs.canBeFoundWith(DiscoveryMode.BLE)) {
                        return 1;
                    }
                    return 0;
                }
            });

            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        baseAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
    };

    private Handler scanningHandler = new Handler();

    private Runnable scanningRunnable = new Runnable() {
        @Override
        public void run() {
            stopScan();
        }
    };

    private BaseAdapter baseAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            RPDeviceCell view = (RPDeviceCell) convertView;

            if (view == null) {
                view = new RPDeviceCell(getActivity());
            }

            Device model = (Device) getItem(position);

            view.setDeviceName(model.getDeviceName());
            if (model instanceof BleDevice) {
                view.setDeviceModel(model.getDeviceId().toString());
            } else {
                view.setDeviceModel(((MdnsDevice) model).getAddr());
            }
            view.setDeviceState(model.canBeFoundWith(DiscoveryMode.MDNS) ? "Waiting to connect" : "New device");

            return view;
        }
    };


    public ConnectFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_connect, container, false);

        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startScan();
            }
        });

        ListView listDevices = (ListView) rootView.findViewById(R.id.list_devices);

        listDevices.setAdapter(baseAdapter);

        listDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Device object = devices.get(position);
                if (object.canBeFoundWith(DiscoveryMode.BLE)) {
                    showWifiDialog((BleDevice) object);
                } else if (object.canBeFoundWith(DiscoveryMode.MDNS)) {
                    MdnsDevice device = (MdnsDevice) object;
                    mListener.connectDevice(device.getAddr(), device.getPort());
                }
            }
        });

        Button buttonConnectByIp = (Button) rootView.findViewById(R.id.button_manul_connect);
        buttonConnectByIp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestAddress();
            }
        });

        deviceManager = new DeviceManager(getActivity());
        deviceManager.setListener(discoveryListener);

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();

        devices.clear();
        baseAdapter.notifyDataSetChanged();

        stopScan();
    }

    @Override
    public void onResume() {
        super.onResume();

        startScan();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        deviceManager = null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof IConnectFragmentInteractionListener) {
            mListener = (IConnectFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() +
                    "must implement IConnectFragmentInteractionListener");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof IConnectFragmentInteractionListener) {
            mListener = (IConnectFragmentInteractionListener) activity;
        } else {
            throw new RuntimeException(activity.toString() +
                    "must implement IConnectFragmentInteractionListener");
        }
    }

    private void startScan() {
        if (!scanning) {
            scanning = true;
            devices.clear();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    baseAdapter.notifyDataSetChanged();
                }
            });
            swipeRefreshLayout.setRefreshing(true);
            scanningHandler.postDelayed(scanningRunnable, 1000 * 30);

            deviceManager.start(DiscoveryMode.BLE);
            deviceManager.start(DiscoveryMode.MDNS);
        }
    }

    private void stopScan() {
        scanning = false;
        swipeRefreshLayout.setRefreshing(false);
        scanningHandler.removeCallbacks(scanningRunnable);

        deviceManager.stop(DiscoveryMode.BLE);
        deviceManager.stop(DiscoveryMode.MDNS);
    }

    private void showWifiDialog(final BleDevice device) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View root = getActivity().getLayoutInflater().inflate(R.layout.dialog_wifi, null);
        final EditText inputSsid = (EditText) root.findViewById(R.id.input_ssid);
        final EditText inputPwd = (EditText) root.findViewById(R.id.input_pwd);

        builder.setView(root);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String ssid = inputSsid.getText().toString();
                String pwd = inputPwd.getText().toString();

                configureWifi(device, ssid, pwd);

                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void configureWifi(BleDevice device, String ssid, String pwd) {
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage("Setup");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        deviceManager.pair(device, ssid, pwd, new AbstractDiscover.BleConfigureListener() {
            @Override
            public void onConfigureSuccess() {
                dialog.dismiss();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(),
                                "Configure successfully", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onConfigureFailure(String error) {
                dialog.dismiss();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(),
                                "Wrong ssid or password", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void requestAddress() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View root = getActivity().getLayoutInflater().inflate(R.layout.dialog_request_address, null);
        final EditText inputAddress = (EditText) root.findViewById(R.id.input_ip_address);
//        final EditText inputPort = (EditText) root.findViewById(R.id.input_port);

        builder.setView(root);

        builder.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String addr = inputAddress.getText().toString();
                if (TextUtils.isEmpty(addr)) {
                    return;
                }

//                String portStr = inputPort.getText().toString();
                int port = 1448;
//                if (!TextUtils.isEmpty(portStr) && TextUtils.isDigitsOnly(portStr)) {
//                    port = Integer.parseInt(portStr);
//                }

                mListener.connectDevice(addr, port);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    public interface IConnectFragmentInteractionListener {
        void connectDevice(String addr, int port);
    }
}
