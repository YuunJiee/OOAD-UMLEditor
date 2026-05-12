package model;

public class GeneralizationLink extends LinkObject {

    public GeneralizationLink(BasicObject from, int fromPort, BasicObject to, int toPort) {
        super(from, fromPort, to, toPort, new HollowTriangleStyle());
    }
}
