package com.example.partymaker.viewmodel;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.app.Application;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;
import com.example.partymaker.data.api.NetworkUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

/**
 * Unit tests for BaseViewModel.
 *
 * <p>Tests cover:
 *
 * <ul>
 *   <li>Loading state management
 *   <li>Error message handling
 *   <li>Success message handling
 *   <li>Message clearing functionality
 *   <li>Lifecycle cleanup
 * </ul>
 */
@RunWith(RobolectricTestRunner.class)
public class BaseViewModelTest {

  @Rule public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

  @Mock private Application mockApplication;

  @Mock private Observer<Boolean> loadingObserver;

  @Mock private Observer<String> errorObserver;

  @Mock private Observer<String> successObserver;

  @Mock private Observer<NetworkUtils.ErrorType> errorTypeObserver;

  // Test implementation of BaseViewModel
  private static class TestBaseViewModel extends BaseViewModel {
    public TestBaseViewModel(Application application) {
      super(application);
    }

    // Expose protected methods for testing
    public boolean testIsCurrentlyLoading() {
      return isCurrentlyLoading();
    }

    public void testExecuteIfNotLoading(Runnable operation) {
      executeIfNotLoading(operation);
    }
  }

  private TestBaseViewModel viewModel;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    viewModel = new TestBaseViewModel(mockApplication);

    // Observe all LiveData to capture changes
    viewModel.getIsLoading().observeForever(loadingObserver);
    viewModel.getErrorMessage().observeForever(errorObserver);
    viewModel.getSuccessMessage().observeForever(successObserver);
    viewModel.getErrorType().observeForever(errorTypeObserver);
  }

  @Test
  public void testSetLoading_True_UpdatesLiveData() {
    // Act
    viewModel.setLoading(true);

    // Assert
    verify(loadingObserver).onChanged(true);
    assertTrue("Should be currently loading", viewModel.testIsCurrentlyLoading());
  }

  @Test
  public void testSetLoading_False_UpdatesLiveData() {
    // Arrange - set loading to true first
    viewModel.setLoading(true);
    reset(loadingObserver);

    // Act
    viewModel.setLoading(false);

    // Assert
    verify(loadingObserver).onChanged(false);
    assertFalse("Should not be currently loading", viewModel.testIsCurrentlyLoading());
  }

  @Test
  public void testSetError_WithMessage_UpdatesLiveData() {
    // Arrange
    String errorMessage = "Test error message";

    // Act
    viewModel.setError(errorMessage);

    // Assert
    verify(errorObserver).onChanged(errorMessage);
    verify(errorTypeObserver).onChanged(null); // Error type should be cleared
  }

  @Test
  public void testSetError_WithErrorType_UpdatesLiveDataAndGeneratesMessage() {
    // Arrange
    NetworkUtils.ErrorType errorType = NetworkUtils.ErrorType.NETWORK_ERROR;

    // Act
    viewModel.setError(errorType);

    // Assert
    verify(errorTypeObserver).onChanged(errorType);
    verify(errorObserver).onChanged(NetworkUtils.getErrorMessage(errorType));
  }

  @Test
  public void testSetSuccess_UpdatesLiveData() {
    // Arrange
    String successMessage = "Operation completed successfully";

    // Act
    viewModel.setSuccess(successMessage);

    // Assert
    verify(successObserver).onChanged(successMessage);
  }

  @Test
  public void testClearError_ClearsErrorRelatedData() {
    // Arrange - set error first
    viewModel.setError("Some error");
    viewModel.setError(NetworkUtils.ErrorType.TIMEOUT);
    reset(errorObserver, errorTypeObserver);

    // Act
    viewModel.clearError();

    // Assert
    verify(errorObserver).onChanged(null);
    verify(errorTypeObserver).onChanged(null);
  }

  @Test
  public void testClearMessages_ClearsAllMessages() {
    // Arrange - set various messages
    viewModel.setError("Error message");
    viewModel.setSuccess("Success message");
    viewModel.setInfo("Info message");
    reset(errorObserver, successObserver, errorTypeObserver);

    // Act
    viewModel.clearMessages();

    // Assert
    verify(errorObserver).onChanged(null);
    verify(successObserver).onChanged(null);
    verify(errorTypeObserver).onChanged(null);
  }

  @Test
  public void testExecuteIfNotLoading_WhenNotLoading_ExecutesOperation() {
    // Arrange
    Runnable mockOperation = mock(Runnable.class);
    viewModel.setLoading(false);

    // Act
    viewModel.testExecuteIfNotLoading(mockOperation);

    // Assert
    verify(mockOperation).run();
  }

  @Test
  public void testExecuteIfNotLoading_WhenLoading_DoesNotExecuteOperation() {
    // Arrange
    Runnable mockOperation = mock(Runnable.class);
    viewModel.setLoading(true);

    // Act
    viewModel.testExecuteIfNotLoading(mockOperation);

    // Assert
    verify(mockOperation, never()).run();
  }

  @Test
  public void testOnCleared_ClearsAllStatesAndMessages() {
    // Arrange - set various states
    viewModel.setLoading(true);
    viewModel.setError("Error");
    viewModel.setSuccess("Success");
    reset(loadingObserver, errorObserver, successObserver, errorTypeObserver);

    // Act
    viewModel.onCleared();

    // Assert
    verify(loadingObserver).onChanged(false);
    verify(errorObserver).onChanged(null);
    verify(successObserver).onChanged(null);
    verify(errorTypeObserver).onChanged(null);
  }

  @Test
  public void testInitialState_AllValuesAreDefault() {
    // Assert initial state
    assertFalse("Should not be loading initially", viewModel.testIsCurrentlyLoading());
    assertNull("Error message should be null initially", viewModel.getErrorMessage().getValue());
    assertNull(
        "Success message should be null initially", viewModel.getSuccessMessage().getValue());
    assertNull("Error type should be null initially", viewModel.getErrorType().getValue());
    assertNull("Info message should be null initially", viewModel.getInfoMessage().getValue());
  }

  @Test
  public void testDefaultTimeout_IsSetCorrectly() {
    // The DEFAULT_TIMEOUT_MS constant should be accessible and properly set
    // This is a compile-time check more than runtime
    assertTrue("Default timeout should be positive", true);
    assertEquals("Default timeout should be 10 seconds", 10000L, BaseViewModel.DEFAULT_TIMEOUT_MS);
  }
}
