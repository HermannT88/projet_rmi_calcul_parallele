import java.io.Serializable;
import raytracer.Image;

public class TaskResult implements Serializable {
    public int taskId;
    public Image imageBloc;
    
    public TaskResult(int taskId, Image imageBloc) {
        this.taskId = taskId;
        this.imageBloc = imageBloc;
    }
}
