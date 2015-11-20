package test.fuzzy.openglplayground;

import java.util.List;

/**
 * Created by fz on 19.11.15.
 */
public class TrackFollower {
    GlPoint _position;
    GlPoint _positionDelta = new GlPoint();
    int _trackTargetIndex = 0;
    final List<GlPoint> _track;
    float _speed;

    public TrackFollower(final List<GlPoint> route, float speed, int startPointIndex) {
        assert (route != null);
        assert (route.size() > startPointIndex);

        _track = route;
        _speed = speed;

        _position = new GlPoint(_track.get(startPointIndex));
        _trackTargetIndex = getNextLoopIdx(_track, startPointIndex);
    }

    GlPoint position() {
        return new GlPoint(_position);
    }

    @Override
    public String toString() {
        return "At: " + _position + "; going to: " + _trackTargetIndex + "; with speed: " + _positionDelta;
    }

    public void tick() {
        _position.add(_positionDelta);

        GlPoint target = _track.get(getNextLoopIdx(_track, _trackTargetIndex));

        if(_positionDelta.isZero() || GlPoint.dist(_position, target) < _speed ) {
            float distance = 0;
            GlPoint src;
            GlPoint dst;
            do {
                _trackTargetIndex = getNextLoopIdx(_track, _trackTargetIndex);
                _position.set(_track.get(_trackTargetIndex));

                src = _track.get(_trackTargetIndex);
                dst = _track.get(getNextLoopIdx(_track, _trackTargetIndex));
                float segmentDistance = GlPoint.dist(src, dst);
                if(segmentDistance == 0) {
                    segmentDistance = 0.0001f;
                }

                distance += segmentDistance;
            }
            while(distance < _speed);

            _positionDelta.x = (dst.x - src.x) / (distance / _speed);
            _positionDelta.y = (dst.y - src.y) / (distance / _speed);
            _positionDelta.z = (dst.z - src.z) / (distance / _speed);
        }
    }

    private int getNextLoopIdx(final List<?> list, final int currentPoint) {
        if(list.size() < 1 ) {
            return -1;
        }
        int dstPointIdx = currentPoint + 1;
        if(dstPointIdx >= list.size()) {
            dstPointIdx = 0;
        }
        return dstPointIdx;
    }
}
