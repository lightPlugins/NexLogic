package io.nexstudios.nexlogic.common.services.placeholder.loader;

import io.nexstudios.nexlogic.common.placeholder.loader.PlaceholderFileModel;
import io.nexstudios.serviceregistry.di.Service;

import java.util.List;

public interface PlaceholderYamlLoaderService extends Service {

  List<PlaceholderFileModel> loadAll();
}