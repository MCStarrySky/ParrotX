package org.serverct.parrot.parrotx.data.flags;

import lombok.NonNull;
import org.serverct.parrot.parrotx.data.PID;

public interface Uniqued {
    PID getID();

    void setID(@NonNull PID pid);

    default boolean check(String id) {
        return getID().getId().equals(id);
    }
}
