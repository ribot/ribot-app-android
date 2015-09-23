package io.ribot.app.test.common.injection.component;

import javax.inject.Singleton;

import dagger.Component;
import io.ribot.app.injection.component.ApplicationComponent;
import io.ribot.app.test.common.injection.module.ApplicationTestModule;

@Singleton
@Component(modules = ApplicationTestModule.class)
public interface TestComponent extends ApplicationComponent {

}
