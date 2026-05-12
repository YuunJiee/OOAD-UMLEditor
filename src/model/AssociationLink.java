package model;

public class AssociationLink extends LinkObject {

    public AssociationLink(BasicObject from, int fromPort, BasicObject to, int toPort) {
        super(from, fromPort, to, toPort, new OpenArrowStyle());
    }
}
