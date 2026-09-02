package com.btxtech.server.service;

import com.btxtech.server.model.ui.BabylonMaterialEntity;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The boundary between "you asked for something that is not there" and "something here is broken".
 * <p>
 * Both used to be a 500 with a stack trace, because this class ended its lookup in a bare
 * {@code Optional.orElseThrow()}. Seven days of production logs held thirty-three of them, every
 * identifiable one an id the editor asked for and did not have - each costing the same log volume
 * and the same second of attention as a genuine fault.
 */
class AbstractBaseEntityCrudServiceTest {
    @SuppressWarnings("unchecked")
    private final JpaRepository<BabylonMaterialEntity, Integer> repository = mock(JpaRepository.class);

    private final AbstractBaseEntityCrudService<BabylonMaterialEntity> service =
            new AbstractBaseEntityCrudService<>(BabylonMaterialEntity.class, repository) {
            };

    @Test
    void anIdWithNoRowSaysSoInsteadOfLookingLikeAFault() {
        when(repository.findById(any())).thenReturn(Optional.empty());

        NoSuchEntityException thrown = assertThrows(NoSuchEntityException.class,
                () -> service.getEntity(4711));

        // A 404 that does not say what was missing sends the reader back into the code.
        assertEquals("No BabylonMaterialEntity with id 4711", thrown.getMessage());
    }

    @Test
    void deletingSomethingThatIsNotThereIsTheSameAnswer() {
        when(repository.findById(any())).thenReturn(Optional.empty());

        assertThrows(NoSuchEntityException.class, () -> service.delete(4711));
    }

    /**
     * A null id is not a mistake and must not throw. This is how an optional foreign key is
     * mapped - {@code getGroundBabylonMaterialId()} returning null means "no material set", and a
     * dozen entity mappers depend on getting null back rather than an exception. A REST path
     * cannot reach this branch at all: the id arrives as a primitive path variable.
     */
    @Test
    void noIdAtAllIsAnAnswerAndNotAnError() {
        assertNull(service.getEntity(null));
    }

    @Test
    void anIdWithARowStillReturnsIt() {
        BabylonMaterialEntity entity = new BabylonMaterialEntity();
        when(repository.findById(7)).thenReturn(Optional.of(entity));

        assertSame(entity, service.getEntity(7));
    }
}
