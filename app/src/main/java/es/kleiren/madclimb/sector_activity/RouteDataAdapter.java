package es.kleiren.madclimb.sector_activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.kleiren.madclimb.R;
import es.kleiren.madclimb.data_classes.Route;
import es.kleiren.madclimb.data_classes.Sector;
import es.kleiren.madclimb.extra_activities.InfoActivity;
import es.kleiren.madclimb.util.InfoChartUtils;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.view.ColumnChartView;

import static android.content.Context.MODE_PRIVATE;

public class RouteDataAdapter extends RecyclerView.Adapter<RouteDataAdapter.ViewHolder> {
    private final Activity activity;
    private final Boolean isInHistoryFragment;
    private final Fragment fragment;
    private ArrayList<Route> routes;
    private Sector sector;
    private Context context;
    private int mExpandedPosition = -1;
    private ColumnChartData data;

    private DatePickerDialog datePickerDialog;

    public RouteDataAdapter(ArrayList<Route> routes, Activity activity, Sector sector, Boolean isInHistoryFragment, Fragment fragment) {
        this.context = activity;
        this.activity = activity;
        this.routes = routes;
        this.sector = sector;
        this.fragment = fragment;
        this.isInHistoryFragment = isInHistoryFragment;
    }

    @Override
    public RouteDataAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view;
        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.route_row, viewGroup, false);
        return new ViewHolder(view, i == 1);
    }

    @Override
    public void onBindViewHolder(final RouteDataAdapter.ViewHolder viewHolder, final int i) {
        if (!isInHistoryFragment) {
            if (i == 0) {
                if (sector.getRestriction_end() != null) {
                    viewHolder.txtRestricted.setText(String.format(activity.getString(R.string.restricted_climbing), sector.getRestriction_start(), sector.getRestriction_end()));
                }
                generateData();
                viewHolder.chart.setColumnChartData(data);
                viewHolder.btnInfo.setOnClickListener(view -> {
                    Intent intent = new Intent(context, InfoActivity.class);
                    intent.putExtra("type", "sector");
                    intent.putExtra("datum", sector);
                    context.startActivity(intent);
                });
            }
        }

        viewHolder.txtName.setText(routes.get(i).getName());
        viewHolder.txtGrade.setText(routes.get(i).getGrade());
        try {
            viewHolder.txtGrade.setTextColor(InfoChartUtils.colors[InfoChartUtils.map.get(routes.get(i).getGrade()) - 1]);
        } catch (Exception e) {
            viewHolder.txtGrade.setTextColor(Color.GRAY);
        }

        boolean expandable = false, extras = false;
        if (!routes.get(i).getDescription().isEmpty()) {
            viewHolder.txtDetails.setText(routes.get(i).getDescription());
            expandable = true;
            viewHolder.txtDetails.setVisibility(View.VISIBLE);
        } else {
            viewHolder.txtDetails.setVisibility(View.GONE);
        }

        if (routes.get(i).getQd() != 0) {
            viewHolder.txtQuickDraws.setText(Integer.toString(routes.get(i).getQd()));
            expandable = true;
            extras = true;
            viewHolder.layoutTxtQuickDraws.setVisibility(View.VISIBLE);
        } else {
            viewHolder.layoutTxtQuickDraws.setVisibility(View.INVISIBLE);
        }

        if (routes.get(i).getHeight() != 0) {
            viewHolder.txtRouteHeight.setText(Integer.toString(routes.get(i).getHeight()));
            expandable = true;
            extras = true;
            viewHolder.layoutTxtRouteHeight.setVisibility(View.VISIBLE);
        } else {
            viewHolder.layoutTxtRouteHeight.setVisibility(View.INVISIBLE);
        }

        if (!routes.get(i).getEightADotNu().isEmpty()) {
            expandable = true;
            extras = true;
            viewHolder.btnEightADotNu.setVisibility(View.VISIBLE);
        } else {
            viewHolder.btnEightADotNu.setVisibility(View.GONE);
        }

        if (!extras)
            viewHolder.layoutExtras.setVisibility(View.GONE);
        else
            viewHolder.layoutExtras.setVisibility(View.VISIBLE);

        if (expandable) {
            viewHolder.imageArrow.setVisibility(View.VISIBLE);
            final boolean isExpanded = i == mExpandedPosition;
            viewHolder.details.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            viewHolder.imageArrow.setImageDrawable(isExpanded ? context.getResources().getDrawable(R.drawable.arrow_up) : context.getResources().getDrawable(R.drawable.arrow_down));
            viewHolder.itemView.setActivated(isExpanded);
            viewHolder.itemView.setOnClickListener(v -> {
                mExpandedPosition = isExpanded ? -1 : i;
                notifyDataSetChanged();
            });
        } else {
            viewHolder.imageArrow.setVisibility(View.GONE);
        }

        if (routes.get(i).getDoneDate() != null) {
            if (isInHistoryFragment) {
                viewHolder.viewDoneDetails.setVisibility(View.VISIBLE);
                viewHolder.txtDoneAttempt.setText(routes.get(i).getDoneAttempt());
                viewHolder.txtDoneDate.setText(routes.get(i).getDoneDate());
                viewHolder.txtZoneSector.setText(routes.get(i).getZoneName() + " > " + routes.get(i).getSectorName());
                viewHolder.doneCheckBox.setChecked(true);
            } else {
                viewHolder.viewDoneDetailsInSector.setVisibility(View.VISIBLE);
                viewHolder.txtDoneDateInSector.setText(routes.get(i).getDoneDate());
                viewHolder.txtDoneAttemptInSector.setText(routes.get(i).getDoneAttempt());
                viewHolder.doneCheckBox.setChecked(true);
            }
        }
        viewHolder.doneCheckBox.setOnClickListener(v -> {
            if (!viewHolder.doneCheckBox.isChecked())
                showDeleteDoneDialog(i, viewHolder.doneCheckBox);
            else
                showDoneDialog(i, viewHolder.doneCheckBox);
        });

        viewHolder.btnEightADotNu.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(routes.get(i).getEightADotNu()));
            context.startActivity(browserIntent);
        });
    }

    private void showDeleteDoneDialog(int i, CheckBox doneCheckBox) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setPositiveButton("Borrar", (dialog, which) -> {
            Route routeDone = routes.get(i);
            SharedPreferences mPrefs = activity.getSharedPreferences("PREFERENCE", MODE_PRIVATE);
            SharedPreferences.Editor prefsEditor = mPrefs.edit();
            Gson gson = new Gson();
            String jsonSaved = mPrefs.getString("ROUTES_DONE", "");
            if (!jsonSaved.isEmpty()) {

                HashMap<String, Route> hmRoutes;
                Type type = new TypeToken<HashMap<String, Route>>() {
                }.getType();
                hmRoutes = gson.fromJson(jsonSaved, type);

                hmRoutes.remove(routeDone.getRef());

                String json = gson.toJson(hmRoutes);
                prefsEditor.putString("ROUTES_DONE", json);
                prefsEditor.apply();
            }

            dialog.dismiss();
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            doneCheckBox.setChecked(true);
            dialog.dismiss();
        });
        builder.setOnCancelListener(dialog -> {
            doneCheckBox.setChecked(true);
            dialog.dismiss();
        });
        builder.setTitle("Borrar ruta hecha");
        builder.setMessage("¿Seguro que quieres borrar la ruta hecha?");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDoneDialog(int i, CheckBox doneCheckBox) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = activity.getLayoutInflater();
        final View doneView = inflater.inflate(R.layout.dialog_route_done, null);
        TextView dateText = doneView.findViewById(R.id.doneDialog_dateText);
        builder.setTitle(R.string.route_completed);
        builder.setPositiveButton("OK", (dialog, which) -> {
            Route routeDone = routes.get(i);
            routeDone.setDoneDate((String) dateText.getText());
            routeDone.setSectorName(sector.getName());
            routeDone.setZoneName(sector.getZoneName());
            routeDone.setDoneAttempt(((Spinner) doneView.findViewById(R.id.spinDoneRoute)).getSelectedItem().toString());
            SharedPreferences mPrefs = activity.getSharedPreferences("PREFERENCE", MODE_PRIVATE);
            SharedPreferences.Editor prefsEditor = mPrefs.edit();
            Gson gson = new Gson();
            String jsonSaved = mPrefs.getString("ROUTES_DONE", "");

            HashMap<String, Route> hmRoutes = new HashMap<>();

            if (!jsonSaved.isEmpty()) {
                java.lang.reflect.Type type = new TypeToken<HashMap<String, Route>>() {
                }.getType();
                hmRoutes = gson.fromJson(jsonSaved, type);
            }

            hmRoutes.put(routeDone.getRef(), routeDone);
            String json = gson.toJson(hmRoutes);
            prefsEditor.putString("ROUTES_DONE", json);
            prefsEditor.apply();
            dialog.dismiss();
        });
        builder.setView(doneView);
        ((TextView) doneView.findViewById(R.id.doneDialog_routeName)).setText(routes.get(i).getName());
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd/MMM/yyyy");
        String formattedDate = df.format(date);
        dateText.setText(formattedDate);
        dateText.setOnClickListener(v -> showDatePicker(dateText, cal));
        doneView.findViewById(R.id.btnEditDate).setOnClickListener(v -> showDatePicker(dateText, cal));
        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            doneCheckBox.setChecked(false);
            dialog.dismiss();
        });
        builder.setOnCancelListener(dialog -> {
            doneCheckBox.setChecked(false);
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDatePicker(TextView dateText, Calendar cal) {
        datePickerDialog = new DatePickerDialog(
                context, (view, year, month, dayOfMonth) -> dateText.setText(new StringBuilder().append(dayOfMonth).append("/").append(month + 1).append("/").append(year)), cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void generateData() {
        ArrayList<String> grades = new ArrayList<>();
        Integer[] gradesFiltered = new Integer[]{0, 0, 0, 0};

        for (Route route : routes) {
            grades.add(route.getGrade());
            try {
                gradesFiltered[InfoChartUtils.map.get(route.getGrade()) - 1]++;
            } catch (Exception e) {
            }
        }

        int numSubColumns = 1;
        int numColumns = 4;
        List<Column> columns = new ArrayList<Column>();
        List<SubcolumnValue> values;
        for (int i = 0; i < numColumns; ++i) {

            values = new ArrayList<>();
            for (int j = 0; j < numSubColumns; ++j) {
                SubcolumnValue temp = new SubcolumnValue(gradesFiltered[i], InfoChartUtils.colors[i]);
                values.add(temp);
            }

            Column column = new Column(values);
            column.setHasLabelsOnlyForSelected(true);
            columns.add(column);
        }

        data = new ColumnChartData(columns);

        List<AxisValue> axisValues = new ArrayList<AxisValue>();
        for (int i = 0; i < InfoChartUtils.labels.length; i++) {
            axisValues.add(new AxisValue(i, InfoChartUtils.labels[i].toCharArray()));
        }
        Axis axisX = new Axis(axisValues);
        data.setAxisXBottom(axisX);
        data.setAxisYLeft(null);

    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return 1;
        else return 0;
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.routeRow_txtRouteName)
        TextView txtName;
        @BindView(R.id.routeRow_txtRouteGrade)
        TextView txtGrade;
        @BindView(R.id.routeRow_txtDetails)
        TextView txtDetails;
        @BindView(R.id.routeRow_viewDetails)
        View details;
        @BindView(R.id.routeRow_cardView)
        ViewGroup recyclerView;
        @BindView(R.id.routeRow_arrow)
        ImageView imageArrow;
        @BindView(R.id.routeRow_layoutExtras)
        View layoutExtras;
        @BindView(R.id.routeRow_txtRouteHeight)
        TextView txtRouteHeight;
        @BindView(R.id.routeRow_txtQuickDraws)
        TextView txtQuickDraws;
        @BindView(R.id.routeRow_btnEightADotNu)
        Button btnEightADotNu;
        @BindView(R.id.routeRow_layoutTxtQuickDraws)
        View layoutTxtQuickDraws;
        @BindView(R.id.routeRow_layoutTxtRouteHeight)
        View layoutTxtRouteHeight;
        @BindView(R.id.routeRow_doneCheckBox)
        CheckBox doneCheckBox;
        @BindView(R.id.routeRow_txtDoneDate)
        TextView txtDoneDate;
        @BindView(R.id.routeRow_txtDoneDateInSector)
        TextView txtDoneDateInSector;
        @BindView(R.id.routeRow_txtDoneAttemptInSector)
        TextView txtDoneAttemptInSector;
        @BindView(R.id.routeRow_txtDoneAttempt)
        TextView txtDoneAttempt;
        @BindView(R.id.routeRow_txtZoneSector)
        TextView txtZoneSector;
        @BindView(R.id.routeRow_doneDetails)
        View viewDoneDetails;
        @BindView(R.id.routeRow_doneDetailsInSector)
        View viewDoneDetailsInSector;
        ColumnChartView chart;
        ImageButton btnInfo;
        TextView txtRestricted;

        ViewHolder(View view, boolean first) {
            super(view);
            ButterKnife.bind(this, view);
            if (!isInHistoryFragment)
                if (first) {
                    if (sector.getRestriction_end() != null) {
                        ViewStub restrictionStub = view.findViewById(R.id.routeRow_restrictionLayout_stub);
                        View inflatedRestrictionStub = restrictionStub.inflate();
                        txtRestricted = inflatedRestrictionStub.findViewById(R.id.routeRow_restrictionText);
                    }
                    ViewStub chartStub = view.findViewById(R.id.routeRow_infoLayout_stub);
                    View inflatedChartStub = chartStub.inflate();
                    chart = inflatedChartStub.findViewById(R.id.routeRow_gradeChart);
                    btnInfo = inflatedChartStub.findViewById(R.id.routeRow_btnInfo);
                }
            details.setVisibility(View.GONE);
            imageArrow.setVisibility(View.GONE);
            viewDoneDetails.setVisibility(View.GONE);
            viewDoneDetailsInSector.setVisibility(View.GONE);
        }
    }
}