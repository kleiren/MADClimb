package es.kleiren.madclimb.util;

import android.content.Context;

import com.heinrichreimersoftware.androidissuereporter.IssueReporterLauncher;

import es.kleiren.madclimb.R;

public class Miscellaneous {

    public static void sendReport(Context context, String origin) {
        IssueReporterLauncher.forTarget("kleiren", "MADClimb")
                .theme(R.style.Theme_IssueReporter_Custom)
                .guestToken("7739a9a2dbe86ab1fa5273d6d0b5485d93745724")
                .minDescriptionLength(20)
                .putExtraInfo("Origin", origin)
                .launch(context);
    }
}
