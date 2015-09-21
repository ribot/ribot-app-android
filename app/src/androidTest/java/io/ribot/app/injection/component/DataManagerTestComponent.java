package io.ribot.app.injection.component;

import dagger.Component;
import io.ribot.app.injection.module.DataManagerTestModule;
import io.ribot.app.injection.scope.PerDataManager;

@PerDataManager
@Component(dependencies = TestComponent.class, modules = DataManagerTestModule.class)
public interface DataManagerTestComponent extends DataManagerComponent {
}
