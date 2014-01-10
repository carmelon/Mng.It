package il.ac.shenkar.mngit;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Ori on 1/10/14.
 */
public class CreateTaskActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new CreateTaskFragment())
                    .commit();
        }
    }

    /**
     * A fragment containing the task creation.
     */
    public static class CreateTaskFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_create, container, false);

            Button createTaskButton = (Button) rootView.findViewById(R.id.createButton);
            createTaskButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText editText = (EditText) v.findViewById(R.id.edit_message);
                    String message = editText.getText().toString();

                    setTask(new TaskDetails(message));
                    getActivity().finish();
                }
            });

            return rootView;
        }

        public void setTask(TaskDetails task)
        {
            task.setId(TaskListDB.getInstance(getActivity()).addTask(task));
        }
    }
}
