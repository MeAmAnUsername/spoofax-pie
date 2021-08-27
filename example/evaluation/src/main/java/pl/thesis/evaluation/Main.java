package pl.thesis.evaluation;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.MapTaskDefs;
import mb.pie.api.MixedSession;
import mb.pie.api.None;
import mb.pie.api.Pie;
import mb.pie.api.TaskDef;
import mb.pie.runtime.PieBuilderImpl;

public class Main {
    public static void main(String[] args) {
        TaskDef<None, None> main = new Greet();

        Pie pie = new PieBuilderImpl()
            .addTaskDefs(new MapTaskDefs(main))
            .build();

        try(MixedSession session = pie.newSession()) {
            session.require(main.createTask(None.instance));
        } catch(ExecException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    static class Greet implements TaskDef<None, None> {
        @java.lang.Override
        public String getId() {
            return getClass().getSimpleName();
        }

        public None exec(ExecContext context, None input) {
            System.out.println("Hello world!");
            return None.instance;
        }
    }
}
