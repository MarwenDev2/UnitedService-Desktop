package tn.test.Controllers;

import org.hibernate.jdbc.Work;
import tn.test.entities.Worker;

public class Shared {
    public static boolean EditMode =false;
    public static Worker worker = null;

    public static Worker getWorker() {
        return worker;
    }

    public static void setWorker(Worker worker) {
        Shared.worker = worker;
    }

    public static boolean isEditMode() {
        return EditMode;
    }

    public static void setEditMode(boolean editMode) {
        EditMode = editMode;
    }
}
