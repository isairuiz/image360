package com.importare.image360;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

//import com.viewpagerindicator.CirclePageIndicator; //CAMBIO se quitó CirclePageIndicator y se puso un customizado IconPageIndicator
import com.viewpagerindicator.IconPagerAdapter; //CAMBIO agregado a implements del adaptador

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private IconPageIndicator mIndicator;
    public Context context;

    private ImageView imagen_btn;
    private int currentPage = 0;
    private static final int[] filesToAttach = {R.drawable.biossmannportada,R.drawable.biossmann1,R.drawable.biossmann2,
            R.drawable.biossmann3,R.drawable.biossmann4,R.drawable.biossmann5,R.drawable.biossmann6,
            R.drawable.biossmann7,R.drawable.biossmann8,R.drawable.biossmann9};
    private static final String[] codigosPaises = {"MX","US","AR","BO","CL","CO"};

	private static final String[] bccParaTodos = {"tarenas@biossmann.com", "ezama@biossmann.com",
			"eespinosa@industrialpolaris.com", "handrade@medicus.com.mx", "storres@medicus.com.mx"};
    private static final String[][] bccMails = { //Se usa la misma lista para todos por lo pronto
			bccParaTodos,
			bccParaTodos,
			bccParaTodos,
			bccParaTodos,
			bccParaTodos,
			bccParaTodos,
			bccParaTodos,
			bccParaTodos,
			bccParaTodos,
			bccParaTodos
    };

    private String Mail_Nombre = "";
    private String Mail_Correo = "";
    private String Mail_Empresa = "";
    private String Mail_Pais = "";
    private String Mail_Code = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/


        context = this;
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentPage = position;
                if(position == 0){
                    imagen_btn.setVisibility(View.VISIBLE);
                }else{
                    imagen_btn.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mIndicator = (IconPageIndicator)findViewById(R.id.indicator);
        mIndicator.setViewPager(mViewPager);



        imagen_btn = (ImageView)findViewById(R.id.imagen_360_btn);

        imagen_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,Image360Activity.class);
                startActivity(intent);
            }
        });


        requestPermisions();
    }
    private void requestPermisions(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
            }
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("activity", "resumiendo");
        hacerFullscreen();
    }

    private void hacerFullscreen(){
        Log.d("activity", "haciendo fullscreen");

        Handler mHandler = new Handler(){ //CAMBIO para asegurar fullscreen después de cerrar Soft Keyboard, se usa delay
            @Override
            public void handleMessage(Message msg){
                if(isFinishing())return;
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                );
            }
        };
        mHandler.sendEmptyMessageDelayed(0,300);
    }

    private void createAlert(){
        final Dialog dialog = new Dialog(context);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_alert);
        Button enviar_btn = (Button) dialog.findViewById(R.id.enviar_btn);
        final EditText nombre_edit = (EditText) dialog.findViewById(R.id.correo_nombre);
        final EditText correo_edit = (EditText) dialog.findViewById(R.id.correo_mail);
        final EditText empresa_edit = (EditText) dialog.findViewById(R.id.correo_empresa);
        final Spinner countrySpinner = (Spinner) dialog.findViewById(R.id.spinner1);

        enviar_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isValid = validateEdits(nombre_edit,correo_edit,empresa_edit,countrySpinner);
                if(isValid){
                    createEmailIntent();
                }
            }
        });
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override public void onShow(DialogInterface dialogInterface){
                hacerFullscreen();
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override public void onDismiss(DialogInterface dialogInterface){
                hacerFullscreen();
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override public void onCancel(DialogInterface dialogInterface){
                hacerFullscreen();
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.argb(170,128,128,128)));
        dialog.show();
    }

    private boolean validateEdits(EditText nombre_edit, EditText correo_edit, EditText empresa_edit, Spinner spinner){
        String nombre = nombre_edit.getText().toString();
        String mail = correo_edit.getText().toString();
        String empresa = empresa_edit.getText().toString();
        Mail_Nombre = nombre;
        Mail_Correo = mail;
        Mail_Empresa = empresa;
        Mail_Pais = spinner.getSelectedItem().toString();
        int paisPosition = spinner.getSelectedItemPosition();
        Mail_Code = codigosPaises[paisPosition];
        String errors = "Campos requeridos: \n";
        boolean hasError = false;
        if(TextUtils.isEmpty(nombre)){
            hasError = true;
            errors += "-Nombre\n";
        }
        if(TextUtils.isEmpty(mail)){
            hasError = true;
            errors += "-Correo\n";
        }else{
            /*validar email*/
            boolean isValid = isValidEmail(Mail_Correo);
            if(!isValid){
                hasError = true;
                errors += "-El correo no es valido\n";
            }
        }
        if(TextUtils.isEmpty(empresa)){
            errors += "-empresa\n";
            hasError = true;
        }
        if(hasError){
            Toast.makeText(context, errors, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private void createEmailIntent(){

        Intent i = new Intent(Intent.ACTION_SEND);

        if(currentPage != 0){
            i.setType("application/image");
            int idImage = filesToAttach[currentPage];
            String nameImage = getDrawableName(idImage);
            createExternalFile(idImage,nameImage,"jpg");
            i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(Environment.getExternalStorageDirectory(), nameImage+".jpg")));
        }else{
            i.setType("application/pdf");
            int idFile = R.raw.catalogo;
            String fileName = getDrawableName(idFile);
            createExternalFile(idFile,fileName,"pdf");
            i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(Environment.getExternalStorageDirectory(), fileName+".pdf")));
        }

        i.putExtra(Intent.EXTRA_EMAIL, new String[] {
                Mail_Correo
        });
        i.putExtra(Intent.EXTRA_BCC, bccMails[currentPage]);
        i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject, Mail_Code, Mail_Empresa));
        i.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_body, Mail_Nombre) );
        //i.putExtra(Intent.EXTRA_TEXT, "Enviando imagen a "+Mail_Nombre+
        //        " de la empresa "+Mail_Empresa+" y pais "+Mail_Pais + " con codigo "+ Mail_Code);

        startActivity(createEmailOnlyChooserIntent(i, "Send via email"));
    }

    private String getDrawableName(int id){
        String nameRes = getResources().getResourceEntryName(id);
        return nameRes;
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException
    {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1)
        {
            out.write(buffer, 0, read);
        }
    }

    private void createExternalFile(int idImage, String name, String ext){
        InputStream in = null;
        OutputStream out = null;
        try
        {
            in = getResources().openRawResource(idImage);
            out = new FileOutputStream(new File(Environment.getExternalStorageDirectory(), name+"."+ext));
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        }
        catch (Exception e)
        {
            Log.e("tag", e.getMessage());
            e.printStackTrace();
        }
    }


    public Intent createEmailOnlyChooserIntent(Intent source,
                                               CharSequence chooserTitle) {
        Stack<Intent> intents = new Stack<Intent>();
        Intent i = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",
                "info@domain.com", null));
        List<ResolveInfo> activities = getPackageManager()
                .queryIntentActivities(i, 0);

        for(ResolveInfo ri : activities) {
            Intent target = new Intent(source);
            target.setPackage(ri.activityInfo.packageName);
            intents.add(target);
        }

        if(!intents.isEmpty()) {
            Intent chooserIntent = Intent.createChooser(intents.remove(0),
                    chooserTitle);
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                    intents.toArray(new Parcelable[intents.size()]));

            return chooserIntent;
        } else {
            return Intent.createChooser(source, chooserTitle);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){ //CAMBIO agregado
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus)
            hacerFullscreen();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter implements IconPagerAdapter { //CAMBIO se agregó implements

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            // Show 11 total pages.
            return 10;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
                case 3:
                    return "SECTION 4";
                case 4:
                    return "SECTION 5";
                case 5:
                    return "SECTION 6";
                case 6:
                    return "SECTION 7";
                case 7:
                    return "SECTION 8";
                case 8:
                    return "SECTION 9";
                case 9:
                    return "SECTION 10";
            }
            return null;
        }

        @Override
        public int getIconResId(int index){ //CAMBIO se agregó esta función (y el resource)
            return R.drawable.selector_circulo;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final int[] backgrounds = {R.drawable.biossmannportada,R.drawable.biossmann1,R.drawable.biossmann2,
                R.drawable.biossmann3,R.drawable.biossmann4,R.drawable.biossmann5,R.drawable.biossmann6,
                R.drawable.biossmann7,R.drawable.biossmann8,R.drawable.biossmann9};

        private Button btn_hide;

        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            int currentPosition = getArguments().getInt(ARG_SECTION_NUMBER);
            int imageId = backgrounds[currentPosition];
            ImageView image = (ImageView) rootView.findViewById(R.id.background_image);
            image.setImageResource(imageId);
            ImageView correo_btn = (ImageView) rootView.findViewById(R.id.enviar_correo_btn);
            correo_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity)getActivity()).createAlert();
                }
            });

            return rootView;
        }
    }
}
