package com.sunshine;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import java.net.Socket;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.SurfaceHolder.Callback;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
//11111111111111111111
public class AndroidVideo extends Activity implements Callback,OnClickListener{
	private SurfaceView mSurfaceView = null;
	private SurfaceHolder mSurfaceHolder = null;
	private Camera mCamera = null;
	private boolean mPreviewRunning = false;
	
	
	private static final int width = 240;//240;  width*height需要在main.xml中修改
	private static final int height = 160;//160;
	private int iTotalFramePerImage=20;//100;//一幅图像分iTotalFramePerImage次发送
	
	private static final int dataLen = width*height*3/2;
	private int iOneFrameData=dataLen/iTotalFramePerImage;
	private int iNumFrame=0;
	//private int SendDataNum=0;
	//private Bitmap[] mBitmap=new Bitmap[2];
	//连接相关
	private EditText remoteIP=null;
	private Button connect=null;  
	private String remoteIPStr=null;

	//视频数据
	private StreamIt streamIt=null;
	public static  Kit kit=null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.println("connecting...");
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.main);
			
		mSurfaceView = (SurfaceView) this.findViewById(R.id.surface_camera);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		remoteIP=(EditText)this.findViewById(R.id.remoteIP);
		connect=(Button)this.findViewById(R.id.connect);
		connect.setOnClickListener(this);
		
		//myimage.setImageResource(R.drawable.me);

	}


	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		if (mPreviewRunning) {
			mCamera.stopPreview();
		}
		Camera.Parameters p = mCamera.getParameters();
		p.setPreviewSize(width, height);
		streamIt=new StreamIt();
		kit=new Kit();
		mCamera.setPreviewCallback(streamIt);

		mCamera.setParameters(p);
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (Exception ex) {
		}
		mCamera.startPreview();
		mPreviewRunning = true;

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mCamera = Camera.open();
	}


	public void surfaceDestroyed(SurfaceHolder holder) {
		mCamera.stopPreview();
		mPreviewRunning = false;
		mCamera.release();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		try {
			super.onConfigurationChanged(newConfig);
			if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			} else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			}
		} catch (Exception ex) {
		}
	}

	class Kit implements Runnable {
		private boolean run=true;
//		private final int dataLen=57600; //307200 OR 230400 76800 OR 57600

		public void run() {
			// TODO Auto-generated method stub
			try {
				Socket socket = new Socket(remoteIPStr, 8899);
				DataOutputStream dos = new DataOutputStream(socket
						.getOutputStream());
				DataInputStream dis = new DataInputStream(socket
						.getInputStream());
				while (run) {
					
					for(iNumFrame=0;iNumFrame<iTotalFramePerImage;iNumFrame++)
					{
						dos.write(streamIt.yuv420sp, iNumFrame*iOneFrameData, iOneFrameData);
						dis.readBoolean();
					}
					Thread.sleep(10);
				}
			} catch (Exception ex) {
				run=false;
				ex.printStackTrace();
			}
		}

	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		if(view==connect){//连接函数
			remoteIPStr=remoteIP.getText().toString();
			new Thread(AndroidVideo.kit).start();
		}
	}
}

class StreamIt implements Camera.PreviewCallback {
	public byte[] yuv420sp =null;
	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		// TODO Auto-generated method stub
		yuv420sp=data;
		
	}
}