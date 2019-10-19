package es.kleiren.madclimb.sector_activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.kleiren.madclimb.extra_activities.InfoActivity;
import es.kleiren.madclimb.R;
import es.kleiren.madclimb.data_classes.Route;
import es.kleiren.madclimb.data_classes.Sector;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;

import static android.content.Context.MODE_PRIVATE;

public class RouteDataAdapter extends RecyclerView.Adapter<RouteDataAdapter.ViewHolder> {
    private final Activity activity;
    private ArrayList<Route> routes;
    private Sector sector;
    private Context context;
    private int mExpandedPosition = -1;
    private ColumnChartData data;
    private ArrayList<String> grades;

    private Map<String, Integer> map = new HashMap<String, Integer>() {{
        put("3", 1);
        put("3+", 1);
        put("IV-", 1);
        put("IV", 1);
        put("IV+", 1);
        put("V-", 1);
        put("V", 1);
        put("V+", 1);
        put("6a", 2);
        put("6a+", 2);
        put("6b", 2);
        put("6b+", 2);
        put("6c", 2);
        put("6c+", 2);
        put("7a", 3);
        put("7a+", 3);
        put("7b", 3);
        put("7b+", 2);
        put("7c", 3);
        put("7c+", 3);
        put("8a", 4);
        put("8a+", 4);
        put("8b", 4);
        put("8b+", 4);
        put("8c", 4);
        put("8c+", 4);
        put("9a", 4);
        put("9a+", 4);
        put("9b", 4);
        put("9b+", 4);
        put("9c", 4);
        put("9c+", 4);
    }};

    private Integer[] gradesFiltered = new Integer[]{0, 0, 0, 0};
    private Integer[] colors;
    private String[] labels;
    private DatePickerDialog datePickerDialog;

    public RouteDataAdapter(ArrayList<Route> routes, Activity activity, Sector sector) {
        this.context = activity;
        this.activity = activity;
        this.routes = routes;
        this.sector = sector;
    }

    @Override
    public RouteDataAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view;
        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.route_row, viewGroup, false);
        return new ViewHolder(view, i == 1);
    }

    @Override
    public void onBindViewHolder(final RouteDataAdapter.ViewHolder viewHolder, final int i) {
        if (i == 0) {
            generateData();
            viewHolder.chart.setColumnChartData(data);
            viewHolder.btnInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, InfoActivity.class);
                    intent.putExtra("type", "sector");
                    intent.putExtra("datum", sector);
                    context.startActivity(intent);
                }
            });
        }

        viewHolder.txtName.setText(routes.get(i).getName());
        viewHolder.txtGrade.setText(routes.get(i).getGrade());
        try {
            viewHolder.txtGrade.setTextColor(colors[map.get(routes.get(i).getGrade()) - 1]);
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
            viewHolder.layoutTxtQuickDraws.setVisibility(View.GONE);
        }

        if (routes.get(i).getHeight() != 0) {
            viewHolder.txtRouteHeight.setText(Integer.toString(routes.get(i).getHeight()));
            expandable = true;
            extras = true;
            viewHolder.layoutTxtRouteHeight.setVisibility(View.VISIBLE);
        } else {
            viewHolder.layoutTxtRouteHeight.setVisibility(View.GONE);
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
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mExpandedPosition = isExpanded ? -1 : i;
                    notifyDataSetChanged();
                }
            });
        } else {
            viewHolder.imageArrow.setVisibility(View.GONE);
        }


        viewHolder.doneCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDoneDialog(i);
            }
        });
    }

    private void showDoneDialog(int i) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = activity.getLayoutInflater();
        final View doneView = inflater.inflate(R.layout.dialog_route_done, null);
        builder.setPositiveButton("Hecho!", null);
        builder.setView(doneView);
        AlertDialog dialog = builder.create();
        ((TextView)doneView.findViewById(R.id.doneDialog_routeName)).setText(routes.get(i).getName());
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd/MMM/yyyy");
        String formattedDate = df.format(date);
        TextView dateText = doneView.findViewById(R.id.doneDialog_dateText);
        dateText.setText(formattedDate);
        dateText.setOnClickListener(v -> {
            datePickerDialog = new DatePickerDialog(
                    context, (view, year, month, dayOfMonth) -> dateText.setText(new StringBuilder().append(dayOfMonth).append("/").append(month + 1).append("/").append(year)), cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });
        dialog.setOnShowListener(dialogInterface -> dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
            SharedPreferences mPrefs = activity.getSharedPreferences("PREFERENCE", MODE_PRIVATE);
            SharedPreferences.Editor prefsEditor = mPrefs.edit();
            Gson gson = new Gson();
            String json = gson.toJson(routes.get(i));

            mPrefs.getString("savedroute", "");
            Route obj = gson.fromJson(json, Route.class);
            if (obj != null)
                Toast.makeText(activity, obj.getName(), Toast.LENGTH_SHORT).show();

            prefsEditor.putString("savedroute", json);
            prefsEditor.commit();
            dialog.dismiss();
        }));
        dialog.show();
    }

    private void generateData() {
        grades = new ArrayList<>();
        gradesFiltered = new Integer[]{0, 0, 0, 0};

        colors = new Integer[]{ChartUtils.COLOR_GREEN, ChartUtils.COLOR_BLUE, ChartUtils.COLOR_VIOLET, ChartUtils.COLOR_RED};
        labels = new String[]{"III - V+", "6a - 6c+", "7a - 7c+", "8a - 9c+"};
        gradesFiltered = new Integer[]{0, 0, 0, 0};

        for (Route route : routes) {
            grades.add(route.getGrade());
            try {
                gradesFiltered[map.get(route.getGrade()) - 1]++;
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
                SubcolumnValue temp = new SubcolumnValue(gradesFiltered[i], colors[i]);
                values.add(temp);
            }

            Column column = new Column(values);
            column.setHasLabelsOnlyForSelected(true);
            columns.add(column);
        }

        data = new ColumnChartData(columns);

        List<AxisValue> axisValues = new ArrayList<AxisValue>();
        for (int i = 0; i < labels.length; i++) {
            axisValues.add(new AxisValue(i, labels[i].toCharArray()));
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
        @BindView(R.id.routeRow_gradeChart)
        ColumnChartView chart;
        @BindView(R.id.routeRow_btnInfo)
        ImageButton btnInfo;
        @BindView(R.id.routeRow_infoLayout)
        ConstraintLayout infoLayout;
        @BindView(R.id.routeRow_arrow)
        ImageView imageArrow;
        @BindView(R.id.routeRow_layoutExtras)
        View layoutExtras;
        @BindView(R.id.routeRow_txtRouteHeight)
        TextView txtRouteHeight;
        @BindView(R.id.routeRow_txtQuickDraws)
        TextView txtQuickDraws;
        @BindView(R.id.routeRow_layoutTxtQuickDraws)
        View layoutTxtQuickDraws;
        @BindView(R.id.routeRow_layoutTxtRouteHeight)
        View layoutTxtRouteHeight;
        @BindView(R.id.routeRow_doneCheckBox)
        CheckBox doneCheckBox;

        ViewHolder(View view, boolean first) {
            super(view);
            ButterKnife.bind(this, view);
            if (first) {
                chart.setZoomEnabled(false);
            } else {
                infoLayout.setVisibility(View.GONE);
            }
            details.setVisibility(View.GONE);
            imageArrow.setVisibility(View.GONE);
        }
    }
}