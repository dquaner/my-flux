package org.xxts.reactor.core.publisher;


/**
 * Reactive Stream signal types
 */
public enum SignalType {

	/**
	 * A signal when the subscription is triggered
	 */
	SUBSCRIBE,
	/**
	 * A signal when a request is made through the subscription
	 */
	REQUEST,
	/**
	 * A signal when the subscription is cancelled
	 */
	CANCEL,
	/**
	 * A signal when an operator receives a subscription
	 */
	ON_SUBSCRIBE,
	/**
	 * A signal when an operator receives an emitted value
	 */
	ON_NEXT,
	/**
	 * A signal when an operator receives an error
	 */
	ON_ERROR,
	/**
	 * A signal when an operator completes
	 */
	ON_COMPLETE,
	/**
	 * A signal when an operator completes
	 */
	AFTER_TERMINATE,
	/**
	 * A context read signal
	 */
	CURRENT_CONTEXT,
	/**
	 * A context update signal
	 */
	ON_CONTEXT;

	@Override
	public String toString() {
        return switch (this) {
            case ON_SUBSCRIBE -> "onSubscribe";
            case ON_NEXT -> "onNext";
            case ON_ERROR -> "onError";
            case ON_COMPLETE -> "onComplete";
            case REQUEST -> "request";
            case CANCEL -> "cancel";
            case CURRENT_CONTEXT -> "currentContext";
            case ON_CONTEXT -> "onContextUpdate";
            case AFTER_TERMINATE -> "afterTerminate";
            default -> "subscribe";
        };
	}
}
