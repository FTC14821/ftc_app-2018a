package scheduling;

public abstract class BackgroundAction extends OngoingAction{
    public BackgroundAction(String label, String descriptionFormat, Object...descriptionArgs){
        super(label, descriptionFormat, descriptionArgs);
    }
}
