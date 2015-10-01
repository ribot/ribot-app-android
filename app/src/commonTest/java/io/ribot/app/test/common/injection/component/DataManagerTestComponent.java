package io.ribot.app.test.common.injection.component;

import dagger.Component;
import io.ribot.app.injection.component.DataManagerComponent;
import io.ribot.app.injection.scope.PerDataManager;
import io.ribot.app.test.common.injection.module.DataManagerTestModule;

@PerDataManager
@Component(dependencies = TestComponent.class, modules = DataManagerTestModule.class)
public interface DataManagerTestComponent extends DataManagerComponent {
}
