package com.chat.Mob;

import android.animation.*;
import android.app.*;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.text.*;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.view.View;
import android.view.View.*;
import android.view.animation.*;
import android.webkit.*;
import android.widget.*;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.unity3d.ads.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.*;
import org.json.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.IUnityAdsListener;


public class HomeActivity extends Activity {
	
	private Timer _timer = new Timer();
	
	private HashMap<String, Object> header = new HashMap<>();
	private HashMap<String, Object> body = new HashMap<>();
	private String comment = "";
	private boolean testMode = false;
	private String unityGameID = "";
	private String placementId = "";
	private HashMap<String, Object> responMap = new HashMap<>();
	private String Prompt = "";
	private String Your_Api = "";
	private String Model = "";
	private double temperature = 0;
	private double max_token = 0;
	private boolean Continues_Conversation = false;
	private String lastConvo = "";
	
	private ArrayList<HashMap<String, Object>> chat_list_map = new ArrayList<>();
	private ArrayList<HashMap<String, Object>> holdMap = new ArrayList<>();
	
	private LinearLayout bg;
	private LinearLayout toolbar;
	private LinearLayout linear3;
	private LinearLayout linear4;
	private ImageView imageview1;
	private TextView title;
	private ListView chat_list_view;
	private LinearLayout mychatbox;
	private ImageView send;
	private EditText mymessage;
	
	private RequestNetwork api;
	private RequestNetwork.RequestListener _api_request_listener;
	private TimerTask typing;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.home);
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
		bg = findViewById(R.id.bg);
		toolbar = findViewById(R.id.toolbar);
		linear3 = findViewById(R.id.linear3);
		linear4 = findViewById(R.id.linear4);
		imageview1 = findViewById(R.id.imageview1);
		title = findViewById(R.id.title);
		chat_list_view = findViewById(R.id.chat_list_view);
		mychatbox = findViewById(R.id.mychatbox);
		send = findViewById(R.id.send);
		mymessage = findViewById(R.id.mymessage);
		api = new RequestNetwork(this);
		
		send.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				Fun.hideKeyboard(getApplicationContext());
				send.setEnabled(false);
				if (mymessage.getText().toString().equals("")) {
						
				}
				else {
						{
								HashMap<String, Object> _item = new HashMap<>();
								_item.put("chat", "[me]".concat(mymessage.getText().toString()));
								chat_list_map.add(_item);
						}
						
						chat_list_view.setAdapter(new Chat_list_viewAdapter(chat_list_map));
						((BaseAdapter)chat_list_view.getAdapter()).notifyDataSetChanged();
						typing = new TimerTask() {
								@Override
								public void run() {
										runOnUiThread(new Runnable() {
												@Override
												public void run() {
														{
																HashMap<String, Object> _item = new HashMap<>();
																_item.put("chat", "[ai]");
																chat_list_map.add(_item);
														}
														
														chat_list_view.setAdapter(new Chat_list_viewAdapter(chat_list_map));
														((BaseAdapter)chat_list_view.getAdapter()).notifyDataSetChanged();
														chat_list_view.smoothScrollToPosition((int)(chat_list_map.size()));
												}
										});
								}
						};
						_timer.schedule(typing, (int)(100));
						header.put("Accept", "application/json");
						header.put("Content-Type", "application/json");
						header.put("Authorization", "Bearer ".concat(Your_Api));
						body.put("model", Model);
						if (Continues_Conversation && true) {
								body.put("prompt", "[ai]".concat(lastConvo.concat("[me]".concat(mymessage.getText().toString().concat("[ai]")))));
						}
						else {
								body.put("prompt", Prompt.concat(mymessage.getText().toString()));
						}
						comment = "You can change max token more than 300 if your response is too short or not completed.";
						body.put("max_tokens", (int)(max_token));
						body.put("temperature", (int)(temperature));
						api.setHeaders(header);
						api.setParams(body, RequestNetworkController.REQUEST_BODY);
						api.startRequestNetwork(RequestNetworkController.POST, "https://api.openai.com/v1/completions", "", _api_request_listener);
						mymessage.setText("");
				}
			}
		});
		
		_api_request_listener = new RequestNetwork.RequestListener() {
			@Override
			public void onResponse(String _param1, String _param2, HashMap<String, Object> _param3) {
				final String _tag = _param1;
				final String _response = _param2;
				final HashMap<String, Object> _responseHeaders = _param3;
				chat_list_map.remove((int)(chat_list_map.size() - 1));
				chat_list_view.setAdapter(new Chat_list_viewAdapter(chat_list_map));
				((BaseAdapter)chat_list_view.getAdapter()).notifyDataSetChanged();
				
				responMap = new Gson().fromJson(_response, new TypeToken<HashMap<String, Object>>(){}.getType());
				final String str = (new Gson()).toJson(responMap.get("choices"), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
				holdMap = new Gson().fromJson(str, new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
				{
						HashMap<String, Object> _item = new HashMap<>();
						_item.put("chat", "[ai]".concat(holdMap.get((int)0).get("text").toString()));
						chat_list_map.add(_item);
				}
				
				chat_list_view.setAdapter(new Chat_list_viewAdapter(chat_list_map));
				((BaseAdapter)chat_list_view.getAdapter()).notifyDataSetChanged();
				chat_list_view.smoothScrollToPosition((int)(chat_list_map.size()));
				send.setEnabled(true);
				lastConvo = lastConvo.concat("[ai]".concat(holdMap.get((int)0).get("text").toString()));
				typing.cancel();
				typing = new TimerTask() {
						@Override
						public void run() {
								runOnUiThread(new Runnable() {
										@Override
										public void run() {
												DisplayInterstitialAd ();
										}
								});
						}
				};
				_timer.schedule(typing, (int)(1000));
			}
			
			@Override
			public void onErrorResponse(String _param1, String _param2) {
				final String _tag = _param1;
				final String _message = _param2;
				{
						HashMap<String, Object> _item = new HashMap<>();
						_item.put("chat", "[ai]".concat(_message));
						chat_list_map.add(_item);
				}
				
				chat_list_view.setAdapter(new Chat_list_viewAdapter(chat_list_map));
				((BaseAdapter)chat_list_view.getAdapter()).notifyDataSetChanged();
				chat_list_view.smoothScrollToPosition((int)(chat_list_map.size()));
				typing.cancel();
				typing = new TimerTask() {
						@Override
						public void run() {
								runOnUiThread(new Runnable() {
										@Override
										public void run() {
												DisplayInterstitialAd ();
										}
								});
						}
				};
				_timer.schedule(typing, (int)(1000));
				send.setEnabled(true);
			}
		};
	}
	
	private void initializeLogic() {
		Prompt = "[me] what is your name?\n[ai] Jade AI. \n[me]";
		Your_Api = "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";
		Model = "text-davinci-003";
		max_token = 300;
		temperature = 0.9d;
		Continues_Conversation = true;
		
		
		
		lastConvo = "";
		testMode = false;
		unityGameID = "5208557";
		placementId = "Interstitial_Android";
		//design
		//modified by Jade Guylan
		if (Build.VERSION.SDK_INT >= 21) { Window w = this.getWindow(); w.setNavigationBarColor(Color.parseColor("#000000")); }
		_Elevation(toolbar, 5);
		title.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/google_sans_medium.ttf"), 1);
		_GradientDrawable(mychatbox, 20, 0, 0, "#424242", "#424242", false, false, 0);
		// Declare a new listener:
		        final UnityAdsListener myAdsListener = new UnityAdsListener ();
		        // Add the listener to the SDK:
		        UnityAds.addListener(myAdsListener);
		        // Initialize the SDK:
		        UnityAds.initialize (this, unityGameID, testMode);
		
		
		
		
		    }
	
	
	
	
	
	// Implement a function to display an ad if the Placement is ready:
	    public void DisplayInterstitialAd () {
			        if (UnityAds.isReady (placementId)) {
					            UnityAds.show (this, placementId);
					        }
			    }
	
	
	
	
	    // Implement the IUnityAdsListener interface methods:
	    private class UnityAdsListener implements IUnityAdsListener {
			
			        @Override
			        public void onUnityAdsReady (String placementId) {
					            // Implement functionality for an ad being ready to show.
					
					
					
					
					//ads ready
					        }
			
			        @Override
			        public void onUnityAdsStart (String placementId) {
					            // Implement functionality for a user starting to watch an ad.
					
					//watching
					        }
			
			        @Override
			        public void onUnityAdsFinish (String placementId, UnityAds.FinishState finishState) {
					            // Implement functionality for a user finishing an ad.
					
					//complete
					        }
			
			        @Override
			        public void onUnityAdsError (UnityAds.UnityAdsError error, String message) {
					            // Implement functionality for a Unityf Ads service error occurring.
					        
					//msg = message;
					//error
			}
	}
	
	public void _GradientDrawable(final View _view, final double _radius, final double _stroke, final double _shadow, final String _color, final String _borderColor, final boolean _ripple, final boolean _clickAnim, final double _animDuration) {
		if (_ripple) {
			android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
			gd.setColor(Color.parseColor(_color));
			gd.setCornerRadius((int)_radius);
			gd.setStroke((int)_stroke,Color.parseColor(_borderColor));
			if (Build.VERSION.SDK_INT >= 21){
				_view.setElevation((int)_shadow);}
			android.content.res.ColorStateList clrb = new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{Color.parseColor("#9E9E9E")});
			android.graphics.drawable.RippleDrawable ripdrb = new android.graphics.drawable.RippleDrawable(clrb , gd, null);
			_view.setClickable(true);
			_view.setBackground(ripdrb);
		}
		else {
			android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
			gd.setColor(Color.parseColor(_color));
			gd.setCornerRadius((int)_radius);
			gd.setStroke((int)_stroke,Color.parseColor(_borderColor));
			_view.setBackground(gd);
			if (Build.VERSION.SDK_INT >= 21){
				_view.setElevation((int)_shadow);}
		}
		if (_clickAnim) {
			_view.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()){
						case MotionEvent.ACTION_DOWN:{
							ObjectAnimator scaleX = new ObjectAnimator();
							scaleX.setTarget(_view);
							scaleX.setPropertyName("scaleX");
							scaleX.setFloatValues(0.9f);
							scaleX.setDuration((int)_animDuration);
							scaleX.start();
							
							ObjectAnimator scaleY = new ObjectAnimator();
							scaleY.setTarget(_view);
							scaleY.setPropertyName("scaleY");
							scaleY.setFloatValues(0.9f);
							scaleY.setDuration((int)_animDuration);
							scaleY.start();
							break;
						}
						case MotionEvent.ACTION_UP:{
							
							ObjectAnimator scaleX = new ObjectAnimator();
							scaleX.setTarget(_view);
							scaleX.setPropertyName("scaleX");
							scaleX.setFloatValues((float)1);
							scaleX.setDuration((int)_animDuration);
							scaleX.start();
							
							ObjectAnimator scaleY = new ObjectAnimator();
							scaleY.setTarget(_view);
							scaleY.setPropertyName("scaleY");
							scaleY.setFloatValues((float)1);
							scaleY.setDuration((int)_animDuration);
							scaleY.start();
							
							break;
						}
					}
					return false;
				}
			});
		}
	}
	
	
	public void _Elevation(final View _view, final double _number) {
		
		_view.setElevation((int)_number);
	}
	
	public class Chat_list_viewAdapter extends BaseAdapter {
		
		ArrayList<HashMap<String, Object>> _data;
		
		public Chat_list_viewAdapter(ArrayList<HashMap<String, Object>> _arr) {
			_data = _arr;
		}
		
		@Override
		public int getCount() {
			return _data.size();
		}
		
		@Override
		public HashMap<String, Object> getItem(int _index) {
			return _data.get(_index);
		}
		
		@Override
		public long getItemId(int _index) {
			return _index;
		}
		
		@Override
		public View getView(final int _position, View _v, ViewGroup _container) {
			LayoutInflater _inflater = getLayoutInflater();
			View _view = _v;
			if (_view == null) {
				_view = _inflater.inflate(R.layout.chat, null);
			}
			
			final LinearLayout linear1 = _view.findViewById(R.id.linear1);
			final LinearLayout linear2 = _view.findViewById(R.id.linear2);
			final LinearLayout ai_all = _view.findViewById(R.id.ai_all);
			final LinearLayout me_all = _view.findViewById(R.id.me_all);
			final LinearLayout linear5 = _view.findViewById(R.id.linear5);
			final LinearLayout ai_box = _view.findViewById(R.id.ai_box);
			final ImageView imageview1 = _view.findViewById(R.id.imageview1);
			final TextView text_ai = _view.findViewById(R.id.text_ai);
			final LinearLayout me_box = _view.findViewById(R.id.me_box);
			final LinearLayout linear6 = _view.findViewById(R.id.linear6);
			final TextView text_me = _view.findViewById(R.id.text_me);
			final ImageView imageview2 = _view.findViewById(R.id.imageview2);
			
			_GradientDrawable(ai_box, 30, 0, 0, "#424242", "#424242", false, false, 0);
			_GradientDrawable(me_box, 30, 0, 0, "#424242", "#424242", false, false, 0);
			if (_data.get((int)_position).get("chat").toString().contains("[ai]")) {
					me_all.setVisibility(View.GONE);
					text_ai.setText(_data.get((int)_position).get("chat").toString().replace("[ai]", ""));
					if (_data.get((int)_position).get("chat").toString().equals("[ai]")) {
							typing = new TimerTask() {
									@Override
									public void run() {
											runOnUiThread(new Runnable() {
													@Override
													public void run() {
															typing = new TimerTask() {
																	@Override
																	public void run() {
																			runOnUiThread(new Runnable() {
																					@Override
																					public void run() {
																							text_ai.setText("•");
																					}
																			});
																	}
															};
															_timer.schedule(typing, (int)(200));
															typing = new TimerTask() {
																	@Override
																	public void run() {
																			runOnUiThread(new Runnable() {
																					@Override
																					public void run() {
																							text_ai.setText("••");
																					}
																			});
																	}
															};
															_timer.schedule(typing, (int)(400));
															typing = new TimerTask() {
																	@Override
																	public void run() {
																			runOnUiThread(new Runnable() {
																					@Override
																					public void run() {
																							text_ai.setText("•••");
																					}
																			});
																	}
															};
															_timer.schedule(typing, (int)(600));
															typing = new TimerTask() {
																	@Override
																	public void run() {
																			runOnUiThread(new Runnable() {
																					@Override
																					public void run() {
																							text_ai.setText("••••");
																					}
																			});
																	}
															};
															_timer.schedule(typing, (int)(800));
													}
											});
									}
							};
							_timer.scheduleAtFixedRate(typing, (int)(1), (int)(1000));
					}
			}
			if (_data.get((int)_position).get("chat").toString().contains("[me]")) {
					ai_all.setVisibility(View.GONE);
					text_me.setText(_data.get((int)_position).get("chat").toString().replace("[me]", ""));
			}
			notifyDataSetChanged();
			
			return _view;
		}
	}
	
	@Deprecated
	public void showMessage(String _s) {
		Toast.makeText(getApplicationContext(), _s, Toast.LENGTH_SHORT).show();
	}
	
	@Deprecated
	public int getLocationX(View _v) {
		int _location[] = new int[2];
		_v.getLocationInWindow(_location);
		return _location[0];
	}
	
	@Deprecated
	public int getLocationY(View _v) {
		int _location[] = new int[2];
		_v.getLocationInWindow(_location);
		return _location[1];
	}
	
	@Deprecated
	public int getRandom(int _min, int _max) {
		Random random = new Random();
		return random.nextInt(_max - _min + 1) + _min;
	}
	
	@Deprecated
	public ArrayList<Double> getCheckedItemPositionsToArray(ListView _list) {
		ArrayList<Double> _result = new ArrayList<Double>();
		SparseBooleanArray _arr = _list.getCheckedItemPositions();
		for (int _iIdx = 0; _iIdx < _arr.size(); _iIdx++) {
			if (_arr.valueAt(_iIdx))
			_result.add((double)_arr.keyAt(_iIdx));
		}
		return _result;
	}
	
	@Deprecated
	public float getDip(int _input) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, _input, getResources().getDisplayMetrics());
	}
	
	@Deprecated
	public int getDisplayWidthPixels() {
		return getResources().getDisplayMetrics().widthPixels;
	}
	
	@Deprecated
	public int getDisplayHeightPixels() {
		return getResources().getDisplayMetrics().heightPixels;
	}
}
