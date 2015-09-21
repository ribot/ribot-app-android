package io.ribot.app.injection.component;

import javax.inject.Singleton;

import dagger.Component;
import io.ribot.app.injection.module.ApplicationTestModule;

@Singleton
@Component(modules = ApplicationTestModule.class)
public interface TestComponent extends ApplicationComponent {

}
