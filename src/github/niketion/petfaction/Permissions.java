package github.niketion.petfaction;

public enum Permissions {
    VIP("petfaction.vip"),
    SIGN("petfaction.sign"),
    COMMAND_SHOP("petfaction.shop"),
    COMMAND_HERE("petfaction.here"),
    COMMAND_AWAY("petfaction.away"),
    COMMAND_NAME("petfaction.name"),
    COMMAND_CHANGE("petfaction.change"),
    COMMAND_GUI("petfaction.gui");

    private String perms;

    Permissions(String perms) {
        this.perms = perms;
    }

    @Override
    public String toString() {
        return perms;
    }
}


